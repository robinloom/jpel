package com.robinloom.jpel.evaluator;

import com.robinloom.jpel.exception.EvaluatorException;
import com.robinloom.jpel.parser.ast.ASTNode;
import com.robinloom.jpel.parser.ast.PathNode;
import com.robinloom.jpel.parser.ast.SortDirection;
import com.robinloom.jpel.parser.ast.SortNode;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class SortEvaluator {

    private final Object root;
    private final ASTNode sourceNode;
    private final Map<String, Object> bindings;

    public SortEvaluator(Object root, ASTNode sourceNode, Map<String, Object> bindings) {
        this.root = root;
        this.sourceNode = sourceNode;
        this.bindings = bindings;
    }

    public Object eval(SortNode sort) {
        Object resolved = resolveSource(sourceNode);

        if (!(resolved instanceof Collection<?> collection)) {
            throw new EvaluatorException(
                "Sort requires a Collection, but got " + resolved.getClass().getSimpleName(),
                null
            );
        }

        List<?> sorted = collection.stream()
            .sorted(createComparator(sort.property(), sort.direction()))
            .collect(Collectors.toList());

        return sorted;
    }

    @SuppressWarnings("unchecked")
    private Comparator<Object> createComparator(String property, SortDirection direction) {
        Comparator<Object> comparator = (a, b) -> {
            Object aValue = resolveProperty(a, property);
            Object bValue = resolveProperty(b, property);

            if (aValue == null && bValue == null) {
                return 0;
            }
            if (aValue == null) {
                return 1;
            }
            if (bValue == null) {
                return -1;
            }

            if (!(aValue instanceof Comparable<?> c)) {
                throw new EvaluatorException(
                    "Sort requires Comparable values, but got " + aValue.getClass().getSimpleName(),
                    null
                );
            }

            try {
                return ((Comparable<Object>) c).compareTo(bValue);
            } catch (ClassCastException e) {
                throw new EvaluatorException(
                    "Type mismatch in sort: cannot compare " + aValue.getClass().getSimpleName() +
                    " with " + bValue.getClass().getSimpleName(),
                    e
                );
            }
        };

        if (direction == SortDirection.DESC) {
            comparator = comparator.reversed();
        }

        return comparator;
    }

    private Object resolveProperty(Object item, String property) {
        Object current = item;
        for (String part : property.split("\\.")) {
            if (current == null) {
                return null;
            }
            current = PropertyResolver.resolve(current, part);
        }
        return current;
    }

    private Object resolveSource(ASTNode source) {
        if (source instanceof SortNode sort) {
            return new SortEvaluator(root, sort.source(), bindings).eval(sort);
        }
        if (source instanceof PathNode path) {
            return new Evaluator(root, path, bindings).eval();
        }
        if (source instanceof com.robinloom.jpel.parser.ast.AggregationNode agg) {
            return new AggregationEvaluator(root, agg.source(), bindings).eval(agg);
        }
        throw new EvaluatorException("Unsupported source type: " + source.getClass().getSimpleName(), null);
    }
}
