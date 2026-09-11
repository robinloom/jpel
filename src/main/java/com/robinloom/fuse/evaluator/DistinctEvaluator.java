package com.robinloom.fuse.evaluator;

import com.robinloom.fuse.exception.EvaluatorException;
import com.robinloom.fuse.parser.ast.AggregationNode;
import com.robinloom.fuse.parser.ast.ASTNode;
import com.robinloom.fuse.parser.ast.DistinctNode;
import com.robinloom.fuse.parser.ast.PathNode;
import com.robinloom.fuse.parser.ast.SortNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class DistinctEvaluator {

    private final Object root;
    private final ASTNode sourceNode;
    private final Map<String, Object> bindings;

    public DistinctEvaluator(Object root, ASTNode sourceNode, Map<String, Object> bindings) {
        this.root = root;
        this.sourceNode = sourceNode;
        this.bindings = bindings;
    }

    public Object eval(DistinctNode distinct) {
        Object resolved = resolveSource(sourceNode);

        if (!(resolved instanceof Collection<?> collection)) {
            throw new EvaluatorException(
                "Distinct requires a Collection, but got " + resolved.getClass().getSimpleName(),
                null
            );
        }

        Set<Object> seenKeys = new LinkedHashSet<>();
        List<Object> result = new ArrayList<>();

        for (Object item : collection) {
            Object key = distinct.property().isPresent()
                ? resolveProperty(item, distinct.property().get())
                : item;

            if (seenKeys.add(key)) {
                result.add(item);
            }
        }

        return result;
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
