package org.acme.rest.json;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Camel route definitions.
 */
public class Routes extends RouteBuilder {
    private final Set<Fruit> fruits = Collections.synchronizedSet(new LinkedHashSet<>());
    private final Set<Vegetable> vegetables = Collections.synchronizedSet(new LinkedHashSet<>());

    public Routes() {

        /* Let's add some initial fruits */
        this.fruits.add(new Fruit("Apple", "Winter fruit"));
        this.fruits.add(new Fruit("Pineapple", "Tropical fruit"));

        /* Let's add some initial vegetables */
        this.vegetables.add(new Vegetable("Carrot", "Root vegetable, usually orange"));
        this.vegetables.add(new Vegetable("Zucchini", "Summer squash"));
    }

    @Override
    public void configure() {
        // @formatter:off
        from("platform-http:/fruits?httpMethodRestrict=GET,POST")
            .choice()
                .when(simple("${header.CamelHttpMethod} == 'GET'"))
                    .setBody().constant(fruits)
                .endChoice()
                .when(simple("${header.CamelHttpMethod} == 'POST'"))
                    .unmarshal()
                        .json(JsonLibrary.Jackson, Fruit.class)
                    .process()
                        .body(Fruit.class, fruits::add)
                    .setBody()
                        .constant(fruits)
                .endChoice()
            .end()
            .marshal().json();

        from("platform-http:/vegetables?httpMethodRestrict=GET")
                .setBody().constant(vegetables)
                .marshal().json()
                .to("seda:vegetables");

        from("seda:vegetables")
                .log("Vegetable received: ${body}");
        // @formatter:on
    }
}
