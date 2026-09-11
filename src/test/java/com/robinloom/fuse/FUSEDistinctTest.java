package com.robinloom.fuse;

import com.robinloom.fuse.fixtures.TestData.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FUSEDistinctTest {

    @Test
    void distinctWithoutPropertyRemovesEqualObjects() {
        Person bob1 = new Person(null, "Bob", 20);
        Person bob2 = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Party party = new Party(List.of(bob1, bob2, alice));

        Object result = FUSE.eval("persons | distinct()", party);
        List<?> distinct = (List<?>) result;

        assertEquals(2, distinct.size());
        assertEquals(bob1, distinct.get(0));
        assertEquals(alice, distinct.get(1));
    }

    @Test
    void distinctByProperty() {
        Person bob = new Person(new Address("Berlin"), "Bob", 20);
        Person alice = new Person(new Address("Berlin"), "Alice", 17);
        Person charlie = new Person(new Address("Essen"), "Charlie", 25);
        Party party = new Party(List.of(bob, alice, charlie));

        Object result = FUSE.eval("persons | distinct(address.city)", party);
        List<?> distinct = (List<?>) result;

        assertEquals(2, distinct.size());
        assertEquals(bob, distinct.get(0));
        assertEquals(charlie, distinct.get(1));
    }

    @Test
    void distinctPreservesFirstOccurrenceOrder() {
        Person charlie = new Person(null, "Charlie", 25);
        Person alice = new Person(null, "Alice", 17);
        Person bob = new Person(null, "Bob", 20);
        Person alice2 = new Person(null, "Alice", 30);
        Party party = new Party(List.of(charlie, alice, bob, alice2));

        Object result = FUSE.eval("persons | distinct(name)", party);
        List<?> distinct = (List<?>) result;

        assertEquals(3, distinct.size());
        assertEquals("Charlie", ((Person) distinct.get(0)).name());
        assertEquals("Alice", ((Person) distinct.get(1)).name());
        assertEquals("Bob", ((Person) distinct.get(2)).name());
        assertEquals(17, ((Person) distinct.get(1)).age());
    }

    @Test
    void distinctOnFilteredSource() {
        Person bob = new Person(new Address("Berlin"), "Bob", 20);
        Person alice = new Person(new Address("Berlin"), "Alice", 17);
        Person charlie = new Person(new Address("Essen"), "Charlie", 25);
        Party party = new Party(List.of(bob, alice, charlie));

        Object result = FUSE.eval("persons[age >= 18] | distinct(address.city)", party);
        List<?> distinct = (List<?>) result;

        assertEquals(2, distinct.size());
    }

    @Test
    void distinctThenSort() {
        Person bob = new Person(new Address("Berlin"), "Bob", 20);
        Person alice = new Person(new Address("Berlin"), "Alice", 17);
        Person charlie = new Person(new Address("Essen"), "Charlie", 25);
        Party party = new Party(List.of(charlie, bob, alice));

        Object result = FUSE.eval("persons | distinct(address.city) | sort(name)", party);
        List<?> distinct = (List<?>) result;

        assertEquals(2, distinct.size());
        assertEquals("Bob", ((Person) distinct.get(0)).name());
        assertEquals("Charlie", ((Person) distinct.get(1)).name());
    }

    @Test
    void distinctThenCount() {
        Person bob = new Person(new Address("Berlin"), "Bob", 20);
        Person alice = new Person(new Address("Berlin"), "Alice", 17);
        Person charlie = new Person(new Address("Essen"), "Charlie", 25);
        Party party = new Party(List.of(bob, alice, charlie));

        Object result = FUSE.eval("persons | distinct(address.city) | count()", party);

        assertEquals(2, result);
    }

    @Test
    void sortThenDistinct() {
        Person bob = new Person(new Address("Berlin"), "Bob", 20);
        Person alice = new Person(new Address("Berlin"), "Alice", 17);
        Person charlie = new Person(new Address("Essen"), "Charlie", 25);
        Party party = new Party(List.of(charlie, bob, alice));

        Object result = FUSE.eval("persons | sort(name) | distinct(address.city)", party);
        List<?> distinct = (List<?>) result;

        assertEquals(2, distinct.size());
        assertEquals("Alice", ((Person) distinct.get(0)).name());
        assertEquals("Charlie", ((Person) distinct.get(1)).name());
    }

    @Test
    void distinctWithParameterBoundFilter() {
        Person bob = new Person(new Address("Berlin"), "Bob", 20);
        Person alice = new Person(new Address("Berlin"), "Alice", 17);
        Person charlie = new Person(new Address("Essen"), "Charlie", 25);
        Party party = new Party(List.of(bob, alice, charlie));

        Expression expr = FUSE.compile("persons[age >= :minAge] | distinct(address.city)");
        Object result = expr.setParameter("minAge", 18).eval(party);
        List<?> distinct = (List<?>) result;

        assertEquals(2, distinct.size());
    }
}
