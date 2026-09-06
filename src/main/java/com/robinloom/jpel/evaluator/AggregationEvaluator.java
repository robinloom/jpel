package com.robinloom.jpel.evaluator;

import com.robinloom.jpel.exception.EvaluatorException;
import com.robinloom.jpel.parser.ast.AggregationNode;
import com.robinloom.jpel.parser.ast.AggregationType;
import com.robinloom.jpel.parser.ast.PathNode;
import com.robinloom.jpel.parser.ast.SegmentNode;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public final class AggregationEvaluator {

    private final Object root;
    private final PathNode pathNode;
    private final Map<String, Object> bindings;

    public AggregationEvaluator(Object root, PathNode pathNode, Map<String, Object> bindings) {
        this.root = root;
        this.pathNode = pathNode;
        this.bindings = bindings;
    }

    public Object eval(AggregationNode aggregation) {
        Object resolved = new Evaluator(root, aggregation.source(), bindings).eval();

        if (!(resolved instanceof Collection<?> collection)) {
            throw new EvaluatorException(
                "Aggregation requires a Collection, but got " + resolved.getClass().getSimpleName(),
                null
            );
        }

        return switch (aggregation.type()) {
            case COUNT -> count(collection);
            case SUM -> sum(collection, aggregation.property());
            case AVG -> avg(collection, aggregation.property());
            case MIN -> min(collection, aggregation.property());
            case MAX -> max(collection, aggregation.property());
        };
    }

    private int count(Collection<?> collection) {
        return collection.size();
    }

    private double sum(Collection<?> collection, Optional<String> property) {
        if (property.isEmpty()) {
            throw new EvaluatorException("sum() requires a property argument", null);
        }
        return collection.stream()
            .mapToDouble(item -> {
                Object value = resolveProperty(item, property.get());
                if (!(value instanceof Number n)) {
                    throw new EvaluatorException(
                        "sum() expects numeric values, but got " + (value != null ? value.getClass().getSimpleName() : "null"),
                        null
                    );
                }
                return n.doubleValue();
            })
            .sum();
    }

    private double avg(Collection<?> collection, Optional<String> property) {
        if (property.isEmpty()) {
            throw new EvaluatorException("avg() requires a property argument", null);
        }
        if (collection.isEmpty()) {
            return 0.0;
        }
        return sum(collection, property) / collection.size();
    }

    @SuppressWarnings("unchecked")
    private Comparable<Object> min(Collection<?> collection, Optional<String> property) {
        if (collection.isEmpty()) {
            return null;
        }
        return collection.stream()
            .map(item -> property.isPresent() ? resolveProperty(item, property.get()) : item)
            .map(v -> {
                if (!(v instanceof Comparable<?> c)) {
                    throw new EvaluatorException(
                        "min() requires Comparable values, but got " + v.getClass().getSimpleName(),
                        null
                    );
                }
                return (Comparable<Object>) c;
            })
            .min(Comparable::compareTo)
            .orElse(null);
    }

    @SuppressWarnings("unchecked")
    private Comparable<Object> max(Collection<?> collection, Optional<String> property) {
        if (collection.isEmpty()) {
            return null;
        }
        return collection.stream()
            .map(item -> property.isPresent() ? resolveProperty(item, property.get()) : item)
            .map(v -> {
                if (!(v instanceof Comparable<?> c)) {
                    throw new EvaluatorException(
                        "max() requires Comparable values, but got " + v.getClass().getSimpleName(),
                        null
                    );
                }
                return (Comparable<Object>) c;
            })
            .max(Comparable::compareTo)
            .orElse(null);
    }

    private Object resolveProperty(Object item, String property) {
        return PropertyResolver.resolve(item, property);
    }
}
