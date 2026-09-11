package com.robinloom.fuse;

import com.robinloom.fuse.fixtures.TestData.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.*;

// Regression tests for gaps found by code review after the flatMap Optional fix:
// Optional-unwrapping is now centralized in PropertyResolver, so every consumer
// (filters, arithmetic, sort, aggregation, distinct) sees it transparently.
class FUSEOptionalUnwrapTest {

    @Test
    void filterOnOptionalTypedProperty() {
        ItemWithOptionalPrice cheap = new ItemWithOptionalPrice("Cheap", Optional.of(5.0));
        ItemWithOptionalPrice expensive = new ItemWithOptionalPrice("Expensive", Optional.of(15.0));
        Cart cart = new Cart(List.of(cheap, expensive));

        assertEquals(List.of(expensive), FUSE.eval("items[price > 10]", cart));
    }

    @Test
    void arithmeticOnOptionalTypedProperty() {
        ItemWithOptionalPrice item = new ItemWithOptionalPrice("Widget", Optional.of(10.0));
        Cart cart = new Cart(List.of(item));

        assertEquals(List.of(item), FUSE.eval("items[price + 5 == 15]", cart));
    }

    @Test
    void filterAppliesThroughOptionalWrappedCollectionProperty() {
        Employee senior = new Employee("Alice", 80000.0);
        Employee junior = new Employee("Bob", 40000.0);
        Team team = new Team("Engineering", Optional.of(List.of(senior, junior)));

        assertEquals(List.of(senior), FUSE.eval("members[salary > 50000]", team));
    }

    @Test
    void indexAppliesThroughOptionalWrappedCollectionProperty() {
        Employee senior = new Employee("Alice", 80000.0);
        Employee junior = new Employee("Bob", 40000.0);
        Team team = new Team("Engineering", Optional.of(List.of(senior, junior)));

        assertEquals(senior, FUSE.eval("members[0]", team));
    }

    @Test
    void emptyOptionalWrappedCollectionResolvesToNull() {
        Team team = new Team("Empty", Optional.empty());

        assertNull(FUSE.eval("members", team));
    }

    @Test
    void primitiveOptionalTypesUnwrapThroughACollectionParent() {
        Metric present = new Metric(OptionalInt.of(3), OptionalLong.of(100L), OptionalDouble.of(1.5));
        Metric empty = new Metric(OptionalInt.empty(), OptionalLong.empty(), OptionalDouble.empty());
        Metrics metrics = new Metrics(List.of(present, empty));

        assertEquals(java.util.Arrays.asList(3, null), FUSE.eval("metrics.quantity", metrics));
        assertEquals(java.util.Arrays.asList(100L, null), FUSE.eval("metrics.total", metrics));
        assertEquals(java.util.Arrays.asList(1.5, null), FUSE.eval("metrics.average", metrics));
    }

    @Test
    void sortByOptionalTypedProperty() {
        ItemWithOptionalPrice cheap = new ItemWithOptionalPrice("Cheap", Optional.of(5.0));
        ItemWithOptionalPrice expensive = new ItemWithOptionalPrice("Expensive", Optional.of(50.0));
        Cart cart = new Cart(List.of(expensive, cheap));

        Object result = FUSE.eval("items | sort(price)", cart);
        assertEquals(List.of(cheap, expensive), result);
    }

    @Test
    void sumOverOptionalTypedProperty() {
        ItemWithOptionalPrice a = new ItemWithOptionalPrice("A", Optional.of(10.0));
        ItemWithOptionalPrice b = new ItemWithOptionalPrice("B", Optional.of(5.0));
        Cart cart = new Cart(List.of(a, b));

        Object result = FUSE.eval("items | sum(price)", cart);
        assertEquals(15.0, (Double) result, 0.01);
    }

    @Test
    void distinctByOptionalTypedProperty() {
        ItemWithOptionalPrice a = new ItemWithOptionalPrice("A", Optional.of(10.0));
        ItemWithOptionalPrice b = new ItemWithOptionalPrice("B", Optional.of(10.0));
        ItemWithOptionalPrice c = new ItemWithOptionalPrice("C", Optional.of(20.0));
        Cart cart = new Cart(List.of(a, b, c));

        Object result = FUSE.eval("items | distinct(price)", cart);
        assertEquals(List.of(a, c), result);
    }
}
