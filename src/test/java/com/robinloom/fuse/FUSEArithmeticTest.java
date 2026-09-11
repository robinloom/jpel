package com.robinloom.fuse;

import com.robinloom.fuse.exception.EvaluatorException;
import com.robinloom.fuse.fixtures.TestData.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FUSEArithmeticTest {

    @Test
    void addition() {
        Item cheap = new Item("Cheap", 5.0, true);
        Item expensive = new Item("Expensive", 50.0, true);
        Store store = new Store(List.of(new Order(1, List.of(cheap, expensive))));

        assertEquals(List.of(expensive), FUSE.eval("orders[0].items[price + 10 > 40]", store));
    }

    @Test
    void subtraction() {
        Deal underpriced = new Deal("Laptop", 800.0, 1000.0);
        Deal overpriced = new Deal("Mouse", 25.0, 20.0);
        Catalog catalog = new Catalog(List.of(underpriced, overpriced));

        assertEquals(List.of(underpriced), FUSE.eval("deals[listPrice - salePrice > 100]", catalog));
    }

    @Test
    void multiplication() {
        Item item1 = new Item("A", 10.0, true);
        Item item2 = new Item("B", 3.0, true);
        Store store = new Store(List.of(new Order(1, List.of(item1, item2))));

        // price * 2 > 15 -> only item1 (10*2=20) qualifies
        assertEquals(List.of(item1), FUSE.eval("orders[0].items[price * 2 > 15]", store));
    }

    @Test
    void division() {
        Deal a = new Deal("A", 50.0, 100.0);
        Deal b = new Deal("B", 90.0, 100.0);
        Catalog catalog = new Catalog(List.of(a, b));

        // salePrice / listPrice < 0.6 -> only A (0.5)
        assertEquals(List.of(a), FUSE.eval("deals[salePrice / listPrice < 0.6]", catalog));
    }

    @Test
    void operatorPrecedenceMultiplicationBeforeAddition() {
        Item item = new Item("Widget", 4.0, true);
        Store store = new Store(List.of(new Order(1, List.of(item))));

        // 2 + price * 2 = 2 + 8 = 10, not (2 + price) * 2 = 12
        assertEquals(List.of(item), FUSE.eval("orders[0].items[2 + price * 2 == 10]", store));
    }

    @Test
    void arithmeticOnLiteralAndParameter() {
        Item item = new Item("Widget", 20.0, true);
        Store store = new Store(List.of(new Order(1, List.of(item))));

        Expression expr = FUSE.compile("orders[0].items[price - :discount > 10]");
        Object result = expr.setParameter("discount", 5.0).eval(store);

        assertEquals(List.of(item), result);
    }

    @Test
    void chainedAdditionAndSubtraction() {
        Item item = new Item("Widget", 10.0, true);
        Store store = new Store(List.of(new Order(1, List.of(item))));

        // 10 + 5 - 3 = 12
        assertEquals(List.of(item), FUSE.eval("orders[0].items[price + 5 - 3 == 12]", store));
    }

    @Test
    void negativeNumbersStillWork() {
        Person bob = new Person(null, "Bob", 20);
        Person alice = new Person(null, "Alice", 17);
        Party party = new Party(List.of(alice, bob));

        assertEquals(bob, FUSE.eval("persons[-1]", party));
        assertEquals(List.of(alice, bob), FUSE.eval("persons[age >= -5]", party));
    }

    @Test
    void filterAppliesAfterFlatteningNestedCollections() {
        // regression test: Evaluator.flatMap() used to silently drop the segment's
        // filter when navigating into a collection-typed property of a collection
        // (e.g. orders.items[...]), returning the unfiltered flattened list instead
        Item item1 = new Item("A", 10.0, true);
        Item item2 = new Item("B", 3.0, true);
        Store store = new Store(List.of(new Order(1, List.of(item1)), new Order(2, List.of(item2))));

        assertEquals(List.of(item1), FUSE.eval("orders.items[price * 2 > 15]", store));
    }

    @Test
    void arithmeticWithNonNumericOperandThrows() {
        Person bob = new Person(null, "Bob", 20);
        Party party = new Party(List.of(bob));

        assertThrows(EvaluatorException.class, () ->
            FUSE.eval("persons[name + 1 == 2]", party));
    }
}
