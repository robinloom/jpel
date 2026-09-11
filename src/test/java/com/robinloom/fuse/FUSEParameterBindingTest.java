package com.robinloom.fuse;

import com.robinloom.fuse.exception.MissingParameterException;
import com.robinloom.fuse.fixtures.TestData.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FUSEParameterBindingTest {

    @Test
    void scalarParameterInComparison() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Party party = new Party(List.of(alice, bob));

        Expression expr = FUSE.compile("persons[age >= :minAge]");
        List<Person> adults = expr.setParameter("minAge", 18).getResultList(party, Person.class);

        assertEquals(List.of(bob), adults);
    }

    @Test
    void reuseExpressionWithDifferentParameters() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person charlie = new Person(null, "Charlie", 25);
        Party party = new Party(List.of(alice, bob, charlie));

        Expression expr = FUSE.compile("persons[age >= :minAge]");

        List<Person> group1 = expr.setParameter("minAge", 18).getResultList(party, Person.class);
        assertEquals(List.of(bob, charlie), group1);

        List<Person> group2 = expr.setParameter("minAge", 21).getResultList(party, Person.class);
        assertEquals(List.of(charlie), group2);
    }

    @Test
    void stringParameterComparison() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Party party = new Party(List.of(alice, bob));

        Expression expr = FUSE.compile("persons[name == :expectedName]");
        Person found = expr.setParameter("expectedName", "Bob").getSingleResult(party, Person.class);

        assertEquals(bob, found);
    }

    @Test
    void literalFirstParameterComparison() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Party party = new Party(List.of(alice, bob));

        Expression expr = FUSE.compile("persons[:minAge <= age]");
        List<Person> adults = expr.setParameter("minAge", 18).getResultList(party, Person.class);

        assertEquals(List.of(bob), adults);
    }

    @Test
    void parameterInListMembership() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person charlie = new Person(null, "Charlie", 25);
        Party party = new Party(List.of(alice, bob, charlie));

        Expression expr = FUSE.compile("persons[name in :allowedNames]");
        List<Person> filtered = expr.setParameter("allowedNames", List.of("Alice", "Charlie"))
                                     .getResultList(party, Person.class);

        assertEquals(List.of(alice, charlie), filtered);
    }

    @Test
    void parameterInNotInListMembership() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person charlie = new Person(null, "Charlie", 25);
        Party party = new Party(List.of(alice, bob, charlie));

        Expression expr = FUSE.compile("persons[name not in :excludedNames]");
        List<Person> filtered = expr.setParameter("excludedNames", List.of("Bob"))
                                     .getResultList(party, Person.class);

        assertEquals(List.of(alice, charlie), filtered);
    }

    @Test
    void multipleParametersInCondition() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person charlie = new Person(null, "Charlie", 25);
        Party party = new Party(List.of(alice, bob, charlie));

        Expression expr = FUSE.compile("persons[age >= :minAge && name != :excluded]");
        List<Person> filtered = expr.setParameter("minAge", 18)
                                     .setParameter("excluded", "Charlie")
                                     .getResultList(party, Person.class);

        assertEquals(List.of(bob), filtered);
    }

    @Test
    void parameterInCollectionOperatorPredicate() {
        Store store = new Store(List.of(
            new Order(1, List.of(new Item("Widget", 50.0, true))),
            new Order(2, List.of(new Item("Gadget", 150.0, true))),
            new Order(3, List.of(new Item("Tool", 25.0, true), new Item("Expensive", 200.0, true)))
        ));

        Expression expr = FUSE.compile("orders[items.any(price > :threshold)]");
        List<Order> expensive = expr.setParameter("threshold", 100.0)
                                     .getResultList(store, Order.class);

        assertEquals(2, expensive.size());
    }

    @Test
    void missingParameterThrowsException() {
        Person bob = new Person(null, "Bob", 20);
        Party party = new Party(List.of(bob));

        Expression expr = FUSE.compile("persons[age >= :minAge]");

        assertThrows(MissingParameterException.class, () -> expr.eval(party));
    }

    @Test
    void missingOneOfMultipleParametersThrowsException() {
        Person bob = new Person(null, "Bob", 20);
        Party party = new Party(List.of(bob));

        Expression expr = FUSE.compile("persons[age >= :minAge && name != :excluded]");
        expr.setParameter("minAge", 18);

        assertThrows(MissingParameterException.class, () -> expr.eval(party));
    }

    @Test
    void literalOnlyExpressionWithoutParametersStillWorks() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Party party = new Party(List.of(alice, bob));

        Expression expr = FUSE.compile("persons[age >= 18]");
        List<Person> adults = expr.getResultList(party, Person.class);

        assertEquals(List.of(bob), adults);
    }

    @Test
    void parameterOverwritePreviousValue() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Party party = new Party(List.of(alice, bob));

        Expression expr = FUSE.compile("persons[age >= :minAge]");

        expr.setParameter("minAge", 25);
        List<Person> empty = expr.getResultList(party, Person.class);
        assertEquals(List.of(), empty);

        expr.setParameter("minAge", 18);
        List<Person> adults = expr.getResultList(party, Person.class);
        assertEquals(List.of(bob), adults);
    }

    @Test
    void parameterWithCombinedLogicalConditions() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person charlie = new Person(null, "Charlie", 25);
        Party party = new Party(List.of(alice, bob, charlie));

        Expression expr = FUSE.compile("persons[age > :minAge && (name == :name1 || name == :name2)]");
        List<Person> filtered = expr.setParameter("minAge", 18)
                                     .setParameter("name1", "Bob")
                                     .setParameter("name2", "Charlie")
                                     .getResultList(party, Person.class);

        assertEquals(List.of(bob, charlie), filtered);
    }

    @Test
    void parameterInStringOperation() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person bobby = new Person(null, "Bobby", 30);
        Party party = new Party(List.of(alice, bob, bobby));

        Expression expr = FUSE.compile("persons[name startsWith :prefix]");
        List<Person> filtered = expr.setParameter("prefix", "Bob").getResultList(party, Person.class);

        assertEquals(List.of(bob, bobby), filtered);
    }
}
