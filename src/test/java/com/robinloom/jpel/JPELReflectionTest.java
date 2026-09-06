package com.robinloom.jpel;

import com.robinloom.jpel.fixtures.TestData.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JPELReflectionTest {

    @Test
    void pojoWithGetters() {
        PersonPOJO alice = new PersonPOJO("Alice", 17);
        PersonPOJO bob = new PersonPOJO("Bob", 20);
        PartyPOJO party = new PartyPOJO(List.of(alice, bob));

        assertEquals("Alice", JPEL.eval("persons[0].name", party));
        assertEquals("Bob", JPEL.eval("persons[1].name", party));
        assertEquals(List.of(bob), JPEL.eval("persons[age >= 18]", party));
    }

    @Test
    void pojoWithPublicFields() {
        PersonWithPublicFields alice = new PersonWithPublicFields("Alice", 17);
        PersonWithPublicFields bob = new PersonWithPublicFields("Bob", 20);
        PartyWithPublicFields party = new PartyWithPublicFields(List.of(alice, bob));

        assertEquals("Alice", JPEL.eval("persons[0].name", party));
        assertEquals(List.of(bob), JPEL.eval("persons[age >= 18]", party));
    }
}
