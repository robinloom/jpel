package com.robinloom.fuse;

import com.robinloom.fuse.exception.NonUniqueResultException;
import com.robinloom.fuse.fixtures.TestData.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FUSEOptionalTest {

    @Test
    void getOptionalResultWithValue() {
        Person bob = new Person(new Address("NYC"), "Bob", 20);
        Party party = new Party(List.of(bob));

        Optional<String> result = FUSE.compile("persons[0].address.city")
            .getOptionalResult(party, String.class);

        assertTrue(result.isPresent());
        assertEquals("NYC", result.get());
    }

    @Test
    void getOptionalResultWithNull() {
        Person bob = new Person(null, "Bob", 20);
        Party party = new Party(List.of(bob));

        Optional<String> result = FUSE.compile("persons[0].address.city")
            .getOptionalResult(party, String.class);

        assertTrue(result.isEmpty());
    }

    @Test
    void getOptionalResultWithEmptyList() {
        Party party = new Party(List.of());

        Optional<Person> result = FUSE.compile("persons[age >= 18]")
            .getOptionalResult(party, Person.class);

        assertTrue(result.isEmpty());
    }

    @Test
    void getOptionalResultWithNoMatch() {
        Person bob = new Person(null, "Bob", 20);
        Party party = new Party(List.of(bob));

        Optional<Person> result = FUSE.compile("persons[age > 30]")
            .getOptionalResult(party, Person.class);

        assertTrue(result.isEmpty());
    }

    @Test
    void getOptionalResultWithMultipleMatches() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Party party = new Party(List.of(alice, bob));

        Expression expr = FUSE.compile("persons[age >= 17]");

        assertThrows(NonUniqueResultException.class,
            () -> expr.getOptionalResult(party, Person.class));
    }

    @Test
    void getOptionalResultWithSingleMatch() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Party party = new Party(List.of(alice, bob));

        Optional<Person> result = FUSE.compile("persons[age == 20]")
            .getOptionalResult(party, Person.class);

        assertTrue(result.isPresent());
        assertEquals("Bob", result.get().name());
    }

    @Test
    void getOptionalResultChaining() {
        Person bob = new Person(new Address("NYC"), "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Party party = new Party(List.of(alice, bob));

        Optional<String> city = FUSE.compile("persons[age >= 18].address.city")
            .getOptionalResult(party, String.class);

        assertEquals("NYC", city.orElse("Unknown"));
    }

    @Test
    void getOptionalResultWithDefault() {
        Person bob = new Person(null, "Bob", 20);
        Party party = new Party(List.of(bob));

        String city = FUSE.compile("persons[0].address.city")
            .getOptionalResult(party, String.class)
            .orElse("Unknown");

        assertEquals("Unknown", city);
    }

    @Test
    void getOptionalResultWithParameter() {
        Person bob = new Person(new Address("NYC"), "Bob", 20);
        Person alice = new Person(new Address("LA"), "Alice", 17);
        Party party = new Party(List.of(alice, bob));

        Optional<String> city = FUSE.compile("persons[name == :targetName].address.city")
            .setParameter("targetName", "Bob")
            .getOptionalResult(party, String.class);

        assertTrue(city.isPresent());
        assertEquals("NYC", city.get());
    }
}
