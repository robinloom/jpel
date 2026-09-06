package com.robinloom.jpath;

import com.robinloom.jpath.fixtures.TestData;
import com.robinloom.jpath.fixtures.TestData.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JPathNavigationTest {

    @Test
    void simplePropertyPath() {
        Person person = new Person(new Address("Essen"), "Alice", 17);
        assertEquals("Essen", JPath.eval("address.city", person));
    }

    @Test
    void propertyPathWithCollection() {
        PersonWithMultipleAddresses person = new PersonWithMultipleAddresses(
                List.of(new Address("Essen"), new Address("Berlin")));
        assertEquals(List.of("Essen", "Berlin"), JPath.eval("address.city", person));
    }

    @Test
    void propertyPathWithCollectionIndex() {
        PersonWithMultipleAddresses person = new PersonWithMultipleAddresses(
                List.of(new Address("Essen"), new Address("Berlin")));
        assertEquals("Essen", JPath.eval("address[0].city", person));
        assertEquals("Berlin", JPath.eval("address[1].city", person));
    }

    @Test
    void returnNullWhenIntermediatePropertyIsNull() {
        Person person = new Person(null, "Alice", 17);
        assertNull(JPath.eval("address.city", person));
    }

    @Test
    void returnNullWhenRootObjectIsNull() {
        assertNull(JPath.eval("address.city", null));
    }

    @Test
    void returnNullWhenLeafPropertyIsNull() {
        Person person = new Person(new Address(null), null, 17);
        assertNull(JPath.eval("address.city", person));
    }

    @Test
    void shouldNavigateOptionalValues() {
        PersonWithOptionalAddress person = new PersonWithOptionalAddress(
                java.util.Optional.of(new Address("Essen")));
        assertEquals("Essen", JPath.eval("address.city", person));
    }

    @Test
    void nestedCollections() {
        Company company = new Company(List.of(
                new Department(List.of(new Address("Essen"), new Address("Berlin"))),
                new Department(List.of(new Address("Hamburg")))));
        assertEquals(List.of("Essen", "Berlin", "Hamburg"),
                JPath.eval("departments.addresses.city", company));
    }

    @Test
    void negativeIndex() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Party party = new Party(List.of(alice, bob));

        assertEquals(bob, JPath.eval("persons[-1]", party));
    }
}
