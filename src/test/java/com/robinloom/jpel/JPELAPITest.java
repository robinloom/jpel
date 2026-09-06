package com.robinloom.jpel;

import com.robinloom.jpel.fixtures.TestData.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JPELAPITest {

    @Test
    void getSingleResult() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Party party = new Party(List.of(alice, bob));

        Expression expression = JPEL.compile("persons[age == 20]");

        assertEquals(bob, expression.getSingleResult(party, Person.class));
    }

    @Test
    void getResultList() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Party party = new Party(List.of(alice, bob));

        Expression expression = JPEL.compile("persons[age == 20]");

        assertEquals(List.of(bob), expression.getResultList(party, Person.class));
    }
}
