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

/**
 * A compiled FUSE expression, obtained via {@link FUSE#compile(String)}.
 * An {@code Expression} can be evaluated repeatedly against different
 * objects and reused with different {@linkplain #setParameter(String, Object)
 * parameter} bindings.
 *
 * <pre>{@code
 * Expression expr = FUSE.compile("persons[age >= :minAge]");
 * List<Person> adults = expr.setParameter("minAge", 18)
 *     .getResultList(data, Person.class);
 * }</pre>
 *
 * @see FUSE
 */
public class Expression {

    private final ASTNode ast;
    private final Map<String, Object> bindings = new HashMap<>();

    Expression(ASTNode ast) {
        this.ast = ast;
    }

    /**
     * Binds a value to a named parameter (e.g. {@code :minAge}) referenced in
     * this expression. Returns {@code this} for fluent chaining.
     *
     * @param name the parameter name, without the leading {@code :}
     * @param value the value to bind; may be a scalar or a {@link java.util.Collection} for {@code in}/{@code not in}
     * @return this expression, for chaining
     */
    public Expression setParameter(String name, Object value) {
        bindings.put(name, value);
        return this;
    }

    /**
     * Evaluates this expression against {@code object} and casts the result
     * to {@code type}, expecting at most one match.
     *
     * @param object the root object to evaluate the expression against
     * @param type the expected result type
     * @param <T> the expected result type
     * @return the single matching result, cast to {@code type}, or {@code null} if the result is an empty collection
     * @throws com.robinloom.fuse.exception.NonUniqueResultException if the expression matches more than one element
     * @throws ClassCastException if the result cannot be cast to {@code type}
     * @throws com.robinloom.fuse.exception.EvaluatorException if evaluation fails, e.g. due to null navigation or a type mismatch
     * @throws com.robinloom.fuse.exception.MissingParameterException if a referenced parameter was never bound via {@link #setParameter}
     */
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

    /**
     * Evaluates this expression against {@code object} and casts every match
     * to {@code type}.
     *
     * @param object the root object to evaluate the expression against
     * @param type the expected element type
     * @param <T> the expected element type
     * @return the matching results, cast to {@code type}; empty if there is no match
     * @throws ClassCastException if a result element cannot be cast to {@code type}
     * @throws com.robinloom.fuse.exception.EvaluatorException if evaluation fails, e.g. due to null navigation or a type mismatch
     * @throws com.robinloom.fuse.exception.MissingParameterException if a referenced parameter was never bound via {@link #setParameter}
     */
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

    /**
     * Evaluates this expression against {@code object} and casts the result
     * to {@code type}, expecting at most one match, wrapping it in an
     * {@link java.util.Optional} instead of returning {@code null}.
     *
     * @param object the root object to evaluate the expression against
     * @param type the expected result type
     * @param <T> the expected result type
     * @return an {@link java.util.Optional} holding the single matching result cast to {@code type}, or empty if there is no match
     * @throws com.robinloom.fuse.exception.NonUniqueResultException if the expression matches more than one element
     * @throws ClassCastException if the result cannot be cast to {@code type}
     * @throws com.robinloom.fuse.exception.EvaluatorException if evaluation fails, e.g. due to null navigation or a type mismatch
     * @throws com.robinloom.fuse.exception.MissingParameterException if a referenced parameter was never bound via {@link #setParameter}
     */
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

    /**
     * Evaluates this expression against {@code object} without casting the
     * result. Navigation and single-value filters yield a scalar (or
     * {@code null}); collection filters yield a {@link List}; {@code sort()},
     * {@code distinct()}, and aggregate pipe operations yield their
     * respective result type.
     *
     * @param object the root object to evaluate the expression against
     * @return the raw evaluation result; may be a scalar, a {@link List}, or {@code null}
     * @throws com.robinloom.fuse.exception.EvaluatorException if evaluation fails, e.g. due to null navigation or a type mismatch
     * @throws com.robinloom.fuse.exception.MissingParameterException if a referenced parameter was never bound via {@link #setParameter}
     */
    public Object eval(Object object) {
        if (ast instanceof AggregationNode aggregation) {
            return new AggregationEvaluator(object, bindings)
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
