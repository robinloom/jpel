package com.robinloom.jpel;

import com.robinloom.jpel.fixtures.TestData.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JPELFilteringTest {

    @Test
    void filteredByNumber() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Party party = new Party(List.of(alice, bob));

        assertEquals(List.of(bob), JPEL.eval("persons[age >= 18]", party));
        assertEquals(List.of(bob), JPEL.eval("persons[age > 19]", party));
        assertEquals(List.of(alice), JPEL.eval("persons[age < 18]", party));
        assertEquals(List.of(alice), JPEL.eval("persons[age <= 17]", party));
        assertEquals(List.of(alice), JPEL.eval("persons[age == 17]", party));
    }

    @Test
    void filteredByString() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Party party = new Party(List.of(alice, bob));

        assertEquals(List.of(alice), JPEL.eval("persons[name == \"Alice\"]", party));
        assertEquals(List.of(bob), JPEL.eval("persons[name != \"Alice\"]", party));
    }

    @Test
    void filteredByBoolean() {
        Car car = new Car("BMW", "X5", true);
        Car car2 = new Car("Audi", "A4", false);
        CarOwner owner = new CarOwner(List.of(car, car2));

        assertEquals(List.of(car), JPEL.eval("cars[electric == true]", owner));
        assertEquals(List.of(car2), JPEL.eval("cars[electric == false]", owner));
    }

    @Test
    void filteredByNull() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(new Address("New York"), "Alice", 17);
        Party party = new Party(List.of(alice, bob));

        assertEquals(List.of(bob), JPEL.eval("persons[address == null]", party));
        assertEquals(List.of(alice), JPEL.eval("persons[address != null]", party));
    }

    @Test
    void filterWithAnd() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Party party = new Party(List.of(alice, bob));

        assertEquals(List.of(alice), JPEL.eval("persons[name == \"Alice\" && age == 17]", party));
    }

    @Test
    void filterWithOr() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person mallory = new Person(null, "Mallory", 21);
        Party party = new Party(List.of(alice, bob, mallory));

        assertEquals(List.of(alice, mallory),
                JPEL.eval("persons[name == \"Alice\" || age == 21]", party));
    }

    @Test
    void filterWithNot() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person mallory = new Person(null, "Mallory", 21);
        Party party = new Party(List.of(alice, bob, mallory));

        assertEquals(List.of(bob, mallory), JPEL.eval("persons[!(age < 18)]", party));
        assertEquals(List.of(alice), JPEL.eval("persons[!(age >= 18)]", party));
        assertEquals(List.of(alice), JPEL.eval("persons[!(age == 20 || age == 21)]", party));
    }

    @Test
    void filterWithNestedProperty() {
        Person alice = new Person(new Address("Berlin"), "Alice", 17);
        Person bob = new Person(new Address("Essen"), "Bob", 20);
        Party party = new Party(List.of(alice, bob));

        assertEquals(List.of(alice), JPEL.eval("persons[address.city == \"Berlin\"]", party));
        assertEquals(List.of(bob), JPEL.eval("persons[address.city != \"Berlin\"]", party));
    }

    @Test
    void filterWithLong() {
        Account alice = new Account("Alice", 5_000_000_000L);
        Account bob = new Account("Bob", 800_000_000L);
        Bank bank = new Bank(List.of(alice, bob));

        assertEquals(List.of(alice), JPEL.eval("accounts[balance > 1000000000]", bank));
        assertEquals(List.of(alice), JPEL.eval("accounts[balance == 5000000000]", bank));
    }

    @Test
    void filterWithDouble() {
        Product cheap = new Product("Coffee", 2.99);
        Product expensive = new Product("Laptop", 999.99);
        Shop shop = new Shop(List.of(cheap, expensive));

        assertEquals(List.of(cheap), JPEL.eval("products[price < 9.99]", shop));
        assertEquals(List.of(expensive), JPEL.eval("products[price >= 999.99]", shop));
    }

    @Test
    void filterWithLiteralFirst() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Party party = new Party(List.of(alice, bob));

        assertEquals(List.of(alice), JPEL.eval("persons[17 == age]", party));
        assertEquals(List.of(bob), JPEL.eval("persons[18 <= age]", party));
        assertEquals(List.of(alice), JPEL.eval("persons[\"Alice\" == name]", party));
    }

    @Test
    void filterWithParentheses() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person mallory = new Person(null, "Mallory", 21);
        Party party = new Party(List.of(alice, bob, mallory));

        assertEquals(List.of(alice, mallory),
                JPEL.eval("persons[(age == 17 || age == 21) && name != \"Bob\"]", party));
        assertEquals(List.of(alice, mallory),
                JPEL.eval("persons[age >= 17 && (name == \"Alice\" || name == \"Mallory\")]", party));
    }

    @Test
    void filterWithContains() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person mallory = new Person(null, "Mallory", 21);
        Party party = new Party(List.of(alice, bob, mallory));

        assertEquals(List.of(alice), JPEL.eval("persons[name contains \"Ali\"]", party));
        assertEquals(List.of(alice), JPEL.eval("persons[name contains \"ice\"]", party));
        assertEquals(List.of(), JPEL.eval("persons[name contains \"xyz\"]", party));
    }

    @Test
    void filterWithStartsWith() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person mallory = new Person(null, "Mallory", 21);
        Party party = new Party(List.of(alice, bob, mallory));

        assertEquals(List.of(alice), JPEL.eval("persons[name startsWith \"A\"]", party));
        assertEquals(List.of(bob), JPEL.eval("persons[name startsWith \"Bo\"]", party));
        assertEquals(List.of(mallory), JPEL.eval("persons[name startsWith \"M\"]", party));
    }

    @Test
    void filterWithEndsWith() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person mallory = new Person(null, "Mallory", 21);
        Party party = new Party(List.of(alice, bob, mallory));

        assertEquals(List.of(alice), JPEL.eval("persons[name endsWith \"e\"]", party));
        assertEquals(List.of(bob), JPEL.eval("persons[name endsWith \"b\"]", party));
        assertEquals(List.of(mallory), JPEL.eval("persons[name endsWith \"y\"]", party));
    }

    @Test
    void filterWithMatches() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person mallory = new Person(null, "Mallory", 21);
        Party party = new Party(List.of(alice, bob, mallory));

        assertEquals(List.of(alice), JPEL.eval("persons[name matches \"A.*\"]", party));
        assertEquals(List.of(alice), JPEL.eval("persons[name matches \"A[a-z]*\"]", party));
        assertEquals(List.of(bob, mallory), JPEL.eval("persons[name matches \"[BM].*\"]", party));
    }

    @Test
    void filterWithStringMatchingAndNullField() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person mallory = new Person(new Address("Berlin"), "Mallory", 21);
        Party party = new Party(List.of(alice, bob, mallory));

        assertEquals(List.of(mallory), JPEL.eval("persons[address.city contains \"e\"]", party));
        assertEquals(List.of(), JPEL.eval("persons[name contains \"xyz\"]", party));
    }

    @Test
    void filterWithStringMatchingMultipleConditions() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person mallory = new Person(null, "Mallory", 21);
        Party party = new Party(List.of(alice, bob, mallory));

        assertEquals(List.of(bob), JPEL.eval("persons[name startsWith \"B\" && age >= 18]", party));
        assertEquals(List.of(alice, mallory), JPEL.eval("persons[name contains \"l\" || name contains \"i\"]", party));
    }

    @Test
    void filterWithIn() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person mallory = new Person(null, "Mallory", 21);
        Party party = new Party(List.of(alice, bob, mallory));

        assertEquals(List.of(alice, mallory), JPEL.eval("persons[name in [\"Alice\", \"Mallory\"]]", party));
        assertEquals(List.of(bob), JPEL.eval("persons[name in [\"Bob\"]]", party));
        assertEquals(List.of(), JPEL.eval("persons[name in [\"Unknown\"]]", party));
    }

    @Test
    void filterWithNotIn() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person mallory = new Person(null, "Mallory", 21);
        Party party = new Party(List.of(alice, bob, mallory));

        assertEquals(List.of(bob, mallory), JPEL.eval("persons[name not in [\"Alice\"]]", party));
        assertEquals(List.of(alice, bob), JPEL.eval("persons[name not in [\"Mallory\"]]", party));
        assertEquals(List.of(alice, bob, mallory), JPEL.eval("persons[name not in [\"Unknown\"]]", party));
    }

    @Test
    void filterWithInNumbers() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person mallory = new Person(null, "Mallory", 21);
        Party party = new Party(List.of(alice, bob, mallory));

        assertEquals(List.of(alice, mallory), JPEL.eval("persons[age in [17, 21]]", party));
        assertEquals(List.of(bob), JPEL.eval("persons[age in [20]]", party));
        assertEquals(List.of(), JPEL.eval("persons[age in [30]]", party));
    }

    @Test
    void filterWithInEmptyList() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Party party = new Party(List.of(alice, bob));

        assertEquals(List.of(), JPEL.eval("persons[name in []]", party));
    }

    @Test
    void filterWithInAndOtherConditions() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Person mallory = new Person(null, "Mallory", 21);
        Party party = new Party(List.of(alice, bob, mallory));

        assertEquals(List.of(mallory), JPEL.eval("persons[name in [\"Alice\", \"Mallory\"] && age >= 20]", party));
        assertEquals(List.of(bob, mallory), JPEL.eval("persons[age >= 20 || name in [\"Bob\"]]", party));
    }
}
