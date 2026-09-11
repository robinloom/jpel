package com.robinloom.fuse;

import com.robinloom.fuse.fixtures.TestData.*;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FUSESortTest {

    @Test
    void sortAscendingNumbers() {
        Shop shop = new Shop(List.of(
            new Product("Widget", 50.0),
            new Product("Gadget", 25.50),
            new Product("Tool", 100.0)
        ));

        Object result = FUSE.eval("products | sort(price)", shop);
        List<?> sorted = (List<?>) result;

        assertEquals(3, sorted.size());
        assertEquals(25.50, ((Product) sorted.get(0)).price(), 0.01);
        assertEquals(50.0, ((Product) sorted.get(1)).price(), 0.01);
        assertEquals(100.0, ((Product) sorted.get(2)).price(), 0.01);
    }

    @Test
    void sortDescendingNumbers() {
        Shop shop = new Shop(List.of(
            new Product("Widget", 50.0),
            new Product("Gadget", 25.50),
            new Product("Tool", 100.0)
        ));

        Object result = FUSE.eval("products | sort(price desc)", shop);
        List<?> sorted = (List<?>) result;

        assertEquals(100.0, ((Product) sorted.get(0)).price(), 0.01);
        assertEquals(50.0, ((Product) sorted.get(1)).price(), 0.01);
        assertEquals(25.50, ((Product) sorted.get(2)).price(), 0.01);
    }

    @Test
    void sortStrings() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person charlie = new Person(null, "Charlie", 25);
        Party party = new Party(List.of(bob, alice, charlie));

        Object result = FUSE.eval("persons | sort(name)", party);
        List<?> sorted = (List<?>) result;

        assertEquals("Alice", ((Person) sorted.get(0)).name());
        assertEquals("Bob", ((Person) sorted.get(1)).name());
        assertEquals("Charlie", ((Person) sorted.get(2)).name());
    }

    @Test
    void sortStringsDescending() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person charlie = new Person(null, "Charlie", 25);
        Party party = new Party(List.of(bob, alice, charlie));

        Object result = FUSE.eval("persons | sort(name desc)", party);
        List<?> sorted = (List<?>) result;

        assertEquals("Charlie", ((Person) sorted.get(0)).name());
        assertEquals("Bob", ((Person) sorted.get(1)).name());
        assertEquals("Alice", ((Person) sorted.get(2)).name());
    }

    @Test
    void sortFiltered() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person charlie = new Person(null, "Charlie", 25);
        Party party = new Party(List.of(alice, bob, charlie));

        Object result = FUSE.eval("persons[age >= 18] | sort(age)", party);
        List<?> sorted = (List<?>) result;

        assertEquals(2, sorted.size());
        assertEquals(20, ((Person) sorted.get(0)).age());
        assertEquals(25, ((Person) sorted.get(1)).age());
    }

    @Test
    void sortExplicitAscending() {
        Shop shop = new Shop(List.of(
            new Product("Widget", 50.0),
            new Product("Gadget", 25.50)
        ));

        Object result = FUSE.eval("products | sort(price asc)", shop);
        List<?> sorted = (List<?>) result;

        assertEquals(25.50, ((Product) sorted.get(0)).price(), 0.01);
        assertEquals(50.0, ((Product) sorted.get(1)).price(), 0.01);
    }

    @Test
    void sortWithNulls() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(new Address("NYC"), "Alice", 17);
        Party party = new Party(List.of(bob, alice));

        Object result = FUSE.eval("persons | sort(address.city)", party);
        List<?> sorted = (List<?>) result;

        assertEquals(2, sorted.size());
        // Nulls should sort last
        assertEquals("NYC", ((Person) sorted.get(0)).address().city());
        assertNull(((Person) sorted.get(1)).address());
    }

    @Test
    void sortThenAggregate() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person charlie = new Person(null, "Charlie", 25);
        Party party = new Party(List.of(alice, bob, charlie));

        // Sort first, then count (sorting doesn't change the count, but tests the chain)
        Object result = FUSE.eval("persons | sort(age) | count()", party);

        assertEquals(3, result);
    }

    @Test
    void sortWithParameter() {
        Shop shop = new Shop(List.of(
            new Product("Widget", 50.0),
            new Product("Gadget", 25.50),
            new Product("Tool", 100.0)
        ));

        Expression expr = FUSE.compile("products[price >= :minPrice] | sort(price desc)");
        Object result = expr.setParameter("minPrice", 40.0).eval(shop);
        List<?> sorted = (List<?>) result;

        assertEquals(2, sorted.size());
        assertEquals(100.0, ((Product) sorted.get(0)).price(), 0.01);
        assertEquals(50.0, ((Product) sorted.get(1)).price(), 0.01);
    }
}
