package com.robinloom.fuse;

import com.robinloom.fuse.evaluator.Evaluator;
import com.robinloom.fuse.evaluator.AggregationEvaluator;
import com.robinloom.fuse.evaluator.DistinctEvaluator;
import com.robinloom.fuse.evaluator.SortEvaluator;
import com.robinloom.fuse.exception.NonUniqueResultException;
import com.robinloom.fuse.parser.ast.ASTNode;
import com.robinloom.fuse.parser.ast.AggregationNode;
import com.robinloom.fuse.parser.ast.DistinctNode;
import com.robinloom.fuse.parser.ast.PathNode;
import com.robinloom.fuse.parser.ast.SortNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Expression {

    private final ASTNode ast;
    private final Map<String, Object> bindings = new HashMap<>();

    public Expression(ASTNode ast) {
        this.ast = ast;
    }

    public Expression setParameter(String name, Object value) {
        bindings.put(name, value);
        return this;
    }

    public <T> T getSingleResult(Object object, Class<T> type) {
        Object result = eval(object);

        if (result instanceof Collection<?> collection) {

            if (collection.isEmpty()) {
                return null;
            }

            if (collection.size() > 1) {
                throw new NonUniqueResultException("Expected a single result, but got " + collection.size());
            }

            return type.cast(collection.iterator().next());
        }

        return type.cast(result);
    }

    public <T> List<T> getResultList(Object object, Class<T> type) {
        Object result = eval(object);

        if (result == null) {
            return List.of();
        }

        if (result instanceof Collection<?> collection) {
            return collection.stream()
                             .map(type::cast)
                             .toList();
        }

        return List.of(type.cast(result));
    }

    public <T> java.util.Optional<T> getOptionalResult(Object object, Class<T> type) {
        Object result = eval(object);

        if (result == null) {
            return java.util.Optional.empty();
        }

        if (result instanceof Collection<?> collection) {
            if (collection.isEmpty()) {
                return java.util.Optional.empty();
            }

            if (collection.size() > 1) {
                throw new NonUniqueResultException("Expected a single result, but got " + collection.size());
            }

            return java.util.Optional.of(type.cast(collection.iterator().next()));
        }

        return java.util.Optional.of(type.cast(result));
    }

    public Object eval(Object object) {
        if (ast instanceof AggregationNode aggregation) {
            return new AggregationEvaluator(object, aggregation.source(), bindings)
                .eval(aggregation);
        }
        if (ast instanceof SortNode sort) {
            return new SortEvaluator(object, sort.source(), bindings)
                .eval(sort);
        }
        if (ast instanceof DistinctNode distinct) {
            return new DistinctEvaluator(object, distinct.source(), bindings)
                .eval(distinct);
        }
        return new Evaluator(object, (PathNode) ast, bindings).eval();
    }
}
