package com.robinloom.fuse;

import com.robinloom.fuse.fixtures.TestData.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FUSEAggregationTest {

    @Test
    void countAll() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person charlie = new Person(null, "Charlie", 25);
        Party party = new Party(List.of(alice, bob, charlie));

        Object result = FUSE.eval("persons | count()", party);

        assertEquals(3, result);
    }

    @Test
    void countFiltered() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person charlie = new Person(null, "Charlie", 25);
        Party party = new Party(List.of(alice, bob, charlie));

        Object result = FUSE.eval("persons[age >= 18] | count()", party);

        assertEquals(2, result);
    }

    @Test
    void sumField() {
        Shop shop = new Shop(List.of(
            new Product("Widget", 50.0),
            new Product("Gadget", 25.50),
            new Product("Tool", 100.0)
        ));

        Object result = FUSE.eval("products | sum(price)", shop);

        assertEquals(175.50, (Double) result, 0.01);
    }

    @Test
    void sumFieldFiltered() {
        Shop shop = new Shop(List.of(
            new Product("Widget", 50.0),
            new Product("Gadget", 25.50),
            new Product("Tool", 100.0)
        ));

        Object result = FUSE.eval("products[price > 30] | sum(price)", shop);

        assertEquals(150.0, (Double) result, 0.01);
    }

    @Test
    void avgField() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person charlie = new Person(null, "Charlie", 25);
        Party party = new Party(List.of(alice, bob, charlie));

        Object result = FUSE.eval("persons | avg(age)", party);

        assertEquals(20.667, (Double) result, 0.01);
    }

    @Test
    void minField() {
        Shop shop = new Shop(List.of(
            new Product("Widget", 50.0),
            new Product("Gadget", 25.50),
            new Product("Tool", 100.0)
        ));

        Object result = FUSE.eval("products | min(price)", shop);

        assertEquals(25.50, (Double) result, 0.01);
    }

    @Test
    void maxField() {
        Shop shop = new Shop(List.of(
            new Product("Widget", 50.0),
            new Product("Gadget", 25.50),
            new Product("Tool", 100.0)
        ));

        Object result = FUSE.eval("products | max(price)", shop);

        assertEquals(100.0, (Double) result, 0.01);
    }

    @Test
    void minStringField() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person charlie = new Person(null, "Charlie", 25);
        Party party = new Party(List.of(alice, bob, charlie));

        Object result = FUSE.eval("persons | min(name)", party);

        assertEquals("Alice", result);
    }

    @Test
    void maxStringField() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person charlie = new Person(null, "Charlie", 25);
        Party party = new Party(List.of(alice, bob, charlie));

        Object result = FUSE.eval("persons | max(name)", party);

        assertEquals("Charlie", result);
    }

    @Test
    void countEmptyResult() {
        Person bob = new Person(null, "Bob", 20);
        Party party = new Party(List.of(bob));

        Object result = FUSE.eval("persons[age > 30] | count()", party);

        assertEquals(0, result);
    }

    @Test
    void avgEmptyResultIsZero() {
        Person bob = new Person(null, "Bob", 20);
        Party party = new Party(List.of(bob));

        Object result = FUSE.eval("persons[age > 30] | avg(age)", party);

        assertEquals(0.0, result);
    }

    @Test
    void aggregationWithParameter() {
        Shop shop = new Shop(List.of(
            new Product("Widget", 50.0),
            new Product("Gadget", 25.50),
            new Product("Tool", 100.0)
        ));

        Expression expr = FUSE.compile("products[price >= :minPrice] | sum(price)");
        Object result = expr.setParameter("minPrice", 30.0).eval(shop);

        assertEquals(150.0, (Double) result, 0.01);
    }
}
