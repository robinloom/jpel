package com.robinloom.jpel;

import com.robinloom.jpel.fixtures.TestData;
import com.robinloom.jpel.fixtures.TestData.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JPELNavigationTest {

    @Test
    void simplePropertyPath() {
        Person person = new Person(new Address("Essen"), "Alice", 17);
        assertEquals("Essen", JPEL.eval("address.city", person));
    }

    @Test
    void propertyPathWithCollection() {
        PersonWithMultipleAddresses person = new PersonWithMultipleAddresses(
                List.of(new Address("Essen"), new Address("Berlin")));
        assertEquals(List.of("Essen", "Berlin"), JPEL.eval("address.city", person));
    }

    @Test
    void propertyPathWithCollectionIndex() {
        PersonWithMultipleAddresses person = new PersonWithMultipleAddresses(
                List.of(new Address("Essen"), new Address("Berlin")));
        assertEquals("Essen", JPEL.eval("address[0].city", person));
        assertEquals("Berlin", JPEL.eval("address[1].city", person));
    }

    @Test
    void returnNullWhenIntermediatePropertyIsNull() {
        Person person = new Person(null, "Alice", 17);
        assertNull(JPEL.eval("address.city", person));
    }

    @Test
    void returnNullWhenRootObjectIsNull() {
        assertNull(JPEL.eval("address.city", null));
    }

    @Test
    void returnNullWhenLeafPropertyIsNull() {
        Person person = new Person(new Address(null), null, 17);
        assertNull(JPEL.eval("address.city", person));
    }

    @Test
    void shouldNavigateOptionalValues() {
        PersonWithOptionalAddress person = new PersonWithOptionalAddress(
                java.util.Optional.of(new Address("Essen")));
        assertEquals("Essen", JPEL.eval("address.city", person));
    }

    @Test
    void nestedCollections() {
        Company company = new Company(List.of(
                new Department(List.of(new Address("Essen"), new Address("Berlin"))),
                new Department(List.of(new Address("Hamburg")))));
        assertEquals(List.of("Essen", "Berlin", "Hamburg"),
                JPEL.eval("departments.addresses.city", company));
    }

    @Test
    void negativeIndex() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Party party = new Party(List.of(alice, bob));

        assertEquals(bob, JPEL.eval("persons[-1]", party));
    }
}
