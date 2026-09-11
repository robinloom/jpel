package com.robinloom.fuse.evaluator;

import com.robinloom.fuse.exception.EvaluatorException;
import com.robinloom.fuse.parser.ast.AggregationNode;
import com.robinloom.fuse.parser.ast.AggregationType;
import com.robinloom.fuse.parser.ast.ASTNode;
import com.robinloom.fuse.parser.ast.DistinctNode;
import com.robinloom.fuse.parser.ast.PathNode;
import com.robinloom.fuse.parser.ast.SortNode;
import com.robinloom.fuse.parser.ast.SegmentNode;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class AggregationEvaluator {

    private final Object root;
    private final ASTNode sourceNode;
    private final Map<String, Object> bindings;

    public AggregationEvaluator(Object root, ASTNode sourceNode, Map<String, Object> bindings) {
        this.root = root;
        this.sourceNode = sourceNode;
        this.bindings = bindings;
    }

    public Object eval(AggregationNode aggregation) {
        Object resolved = resolveSource(aggregation.source());

        if (!(resolved instanceof Collection<?> collection)) {
            throw new EvaluatorException(
                "Aggregation requires a Collection, but got " + resolved.getClass().getSimpleName(),
                null
            );
        }

        return switch (aggregation.type()) {
            case COUNT -> count(collection);
            case COUNT_DISTINCT -> countDistinct(collection, aggregation.property());
            case SUM -> sum(collection, aggregation.property());
            case AVG -> avg(collection, aggregation.property());
            case MIN -> min(collection, aggregation.property());
            case MAX -> max(collection, aggregation.property());
        };
    }

    private int count(Collection<?> collection) {
        return collection.size();
    }

    private int countDistinct(Collection<?> collection, Optional<String> property) {
        Set<Object> seenKeys = new LinkedHashSet<>();
        for (Object item : collection) {
            Object key = property.isPresent() ? resolveProperty(item, property.get()) : item;
            seenKeys.add(key);
        }
        return seenKeys.size();
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

    private Object resolveSource(ASTNode source) {
        if (source instanceof SortNode sort) {
            return new SortEvaluator(root, sort.source(), bindings).eval(sort);
        }
        if (source instanceof DistinctNode distinct) {
            return new DistinctEvaluator(root, distinct.source(), bindings).eval(distinct);
        }
        if (source instanceof PathNode path) {
            return new Evaluator(root, path, bindings).eval();
        }
        if (source instanceof AggregationNode agg) {
            return new AggregationEvaluator(root, agg.source(), bindings).eval(agg);
        }
        throw new EvaluatorException("Unsupported source type: " + source.getClass().getSimpleName(), null);
    }
}
