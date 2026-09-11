package com.robinloom.fuse;

import com.robinloom.fuse.exception.EvaluatorException;
import com.robinloom.fuse.exception.MissingParameterException;
import com.robinloom.fuse.fixtures.TestData.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FUSEErrorMessagesTest {

    @Test
    void typeErrorInComparisonShouldBeDescriptive() {
        Car car = new Car("Toyota", "Prius", true);
        CarOwner owner = new CarOwner(List.of(car));

        Expression expr = FUSE.compile("cars[brand > 5]");

        EvaluatorException e = assertThrows(EvaluatorException.class, () -> expr.eval(owner));
        assertTrue(e.getMessage().contains("comparable") || e.getMessage().contains("String"),
            "Error message should explain type mismatch: " + e.getMessage());
    }

    @Test
    void stringOperationOnNonStringShouldBeDescriptive() {
        Car car = new Car("Toyota", "Prius", true);
        CarOwner owner = new CarOwner(List.of(car));

        Expression expr = FUSE.compile("cars[electric contains \"yes\"]");

        EvaluatorException e = assertThrows(EvaluatorException.class, () -> expr.eval(owner));
        assertTrue(e.getMessage().contains("String"), "Error should mention String requirement: " + e.getMessage());
    }

    @Test
    void listMembershipTestsShouldWork() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Party party = new Party(List.of(alice, bob));

        Expression expr = FUSE.compile("persons[name in [\"Bob\", \"Charlie\"]]");
        List<Person> result = expr.getResultList(party, Person.class);

        assertEquals(List.of(bob), result, "List membership should filter correctly");
    }

    @Test
    void collectionOperationOnNonCollectionShouldBeDescriptive() {
        Person bob = new Person(null, "Bob", 20);
        Party party = new Party(List.of(bob));

        Expression expr = FUSE.compile("persons[age.any(value > 10)]");

        EvaluatorException e = assertThrows(EvaluatorException.class, () -> expr.eval(party));
        assertTrue(e.getMessage().contains("Collection") || e.getMessage().contains("any"),
            "Error should mention collection requirement: " + e.getMessage());
    }

    @Test
    void missingParameterErrorShouldBeSuggestive() {
        Person bob = new Person(null, "Bob", 20);
        Party party = new Party(List.of(bob));

        Expression expr = FUSE.compile("persons[age >= :minAge]");

        MissingParameterException e = assertThrows(MissingParameterException.class, () -> expr.eval(party));
        assertTrue(e.getMessage().contains("minAge") && e.getMessage().contains("setParameter"),
            "Error should name the missing parameter and suggest fix: " + e.getMessage());
    }

    @Test
    void nullComparisonWithComparableOperatorShouldHandleGracefully() {
        Person alice = new Person(null, "Alice", 17);
        Party party = new Party(List.of(alice));

        Expression expr = FUSE.compile("persons[address.city == \"NYC\"]");
        List<Person> result = expr.getResultList(party, Person.class);

        // Navigation to null should just return empty, not throw
        assertEquals(List.of(), result, "Null navigation should return empty results gracefully");
    }

    @Test
    void typeErrorWithStringOperationOnBooleanShouldBeDescriptive() {
        Car car = new Car("Toyota", "Prius", true);
        CarOwner owner = new CarOwner(List.of(car));

        Expression expr = FUSE.compile("cars[electric contains \"abc\"]");

        EvaluatorException e = assertThrows(EvaluatorException.class, () -> expr.eval(owner));
        assertTrue(e.getMessage().contains("String"),
            "Error should mention String requirement: " + e.getMessage());
    }

    @Test
    void validExpressionWithParametersShouldWork() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Party party = new Party(List.of(alice, bob));

        Expression expr = FUSE.compile("persons[age >= :minAge]");
        List<Person> result = expr.setParameter("minAge", 18).getResultList(party, Person.class);

        assertEquals(List.of(bob), result, "Valid expression should still work");
    }
}
