package com.robinloom.fuse.evaluator;

import com.robinloom.fuse.exception.EvaluatorException;
import com.robinloom.fuse.exception.MissingParameterException;
import com.robinloom.fuse.parser.ComparisonOperator;
import com.robinloom.fuse.parser.CollectionOperator;
import com.robinloom.fuse.parser.ast.BinaryLogicalNode;
import com.robinloom.fuse.parser.ast.ConditionNode;
import com.robinloom.fuse.parser.ast.CollectionConditionNode;
import com.robinloom.fuse.parser.ast.LiteralNode;
import com.robinloom.fuse.parser.ast.LogicalNode;
import com.robinloom.fuse.parser.ast.NegationNode;
import com.robinloom.fuse.parser.ast.ParameterNode;
import com.robinloom.fuse.parser.ast.PropertyValueNode;
import com.robinloom.fuse.parser.ast.ValueNode;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;

public final class FilterEvaluator {

    private final Map<String, Object> bindings;

    public FilterEvaluator() {
        this(Map.of());
    }

    public FilterEvaluator(Map<String, Object> bindings) {
        this.bindings = bindings;
    }

    public boolean matches(Object candidate, LogicalNode node) {

        if (node instanceof ConditionNode condition) {
            return evaluateCondition(
                    candidate,
                    condition);
        }

        if (node instanceof BinaryLogicalNode logical) {
            return evaluateLogical(
                    candidate,
                    logical);
        }

        if (node instanceof NegationNode(LogicalNode inner)) {
            return !matches(candidate, inner);
        }

        if (node instanceof CollectionConditionNode(List<String> propertyPath, CollectionOperator operator, LogicalNode predicate)) {
            return evaluateCollectionCondition(candidate, propertyPath, operator, predicate);
        }

        throw new EvaluatorException("Unsupported logical node type: " + node.getClass().getSimpleName(), null);
    }

    private boolean evaluateLogical(Object candidate, BinaryLogicalNode logical) {

        return switch (logical.logicalOperator()) {
            case AND -> matches(candidate, logical.left()) && matches(candidate, logical.right());
            case OR -> matches(candidate, logical.left()) || matches(candidate, logical.right());
        };
    }

    private boolean evaluateCondition(Object candidate, ConditionNode condition) {
        Object left = candidate;
        for (String step : condition.propertyPath()) {
            if (left == null) break;
            left = reflect(left, step);
        }

        Object right = resolveValue(candidate, condition.right());

        return compare(left, condition.comparisonOperator(), right);
    }

    private boolean compare(Object left, ComparisonOperator operator, Object right) {
        return switch (operator) {
            case EQ -> (left instanceof Number l && right instanceof Number r)
                    ? Double.compare(l.doubleValue(), r.doubleValue()) == 0
                    : Objects.equals(left, right);
            case NEQ -> (left instanceof Number l && right instanceof Number r)
                    ? Double.compare(l.doubleValue(), r.doubleValue()) != 0
                    : !Objects.equals(left, right);
            case GT -> compareComparable(left, right) > 0;
            case GTE -> compareComparable(left, right) >= 0;
            case LT -> compareComparable(left, right) < 0;
            case LTE -> compareComparable(left, right) <= 0;
            case CONTAINS -> stringMatch(left, right, String::contains);
            case STARTS_WITH -> stringMatch(left, right, String::startsWith);
            case ENDS_WITH -> stringMatch(left, right, String::endsWith);
            case MATCHES -> stringMatch(left, right, String::matches);
            case IN -> containsValue(left, right);
            case NOT_IN -> !containsValue(left, right);
        };
    }

    @SuppressWarnings("unchecked")
    private int compareComparable(Object left, Object right) {

        if (left == null || right == null) {
            throw new EvaluatorException(
                "Cannot compare null values. Left: " + left + ", Right: " + right,
                null
            );
        }

        if (left instanceof Number l && right instanceof Number r) {
            return Double.compare(l.doubleValue(), r.doubleValue());
        }

        if (!(left instanceof Comparable<?> comparable)) {
            throw new EvaluatorException(
                "Value is not comparable: " + left.getClass().getSimpleName() +
                ". Expected a type implementing Comparable (Number, String, etc.)",
                null
            );
        }

        try {
            return ((Comparable<Object>) comparable).compareTo(right);
        } catch (ClassCastException e) {
            throw new EvaluatorException(
                    "Type mismatch in comparison: cannot compare " + left.getClass().getSimpleName() +
                    " with " + right.getClass().getSimpleName(),
                e
            );
        }
    }

    private Object reflect(Object object, String property) {
        return PropertyResolver.resolve(object, property);
    }

    private boolean stringMatch(Object left, Object right, BiPredicate<String, String> predicate) {
        if (left == null) {
            return false;
        }
        if (!(left instanceof String leftValue)) {
            throw new EvaluatorException(
                "String operation requires a String value, but got " + left.getClass().getSimpleName(),
                null
            );
        }
        if (!(right instanceof String rightValue)) {
            throw new EvaluatorException(
                "String operation requires a String pattern argument, but got " + right.getClass().getSimpleName(),
                null
            );
        }
        return predicate.test(leftValue, rightValue);
    }

    private boolean containsValue(Object left, Object right) {
        if (!(right instanceof List<?> list)) {
            throw new EvaluatorException(
                "List membership test (in/not in) requires a list on the right side, but got " + right.getClass().getSimpleName(),
                null
            );
        }
        if (left instanceof Number leftNumber) {
            return list.stream().anyMatch(v -> v instanceof Number r
                    && Double.compare(leftNumber.doubleValue(), r.doubleValue()) == 0);
        }
        return list.contains(left);
    }

    private boolean evaluateCollectionCondition(Object candidate, List<String> propertyPath,
                                                 CollectionOperator operator, LogicalNode predicate) {
        Object resolved = candidate;
        for (String step : propertyPath) {
            if (resolved == null) break;
            resolved = reflect(resolved, step);
        }

        if (resolved == null) {
            return switch (operator) {
                case ANY -> false;
                case ALL, NONE -> true;
            };
        }

        if (!(resolved instanceof Collection<?> collection)) {
            throw new EvaluatorException(
                "Collection operation (" + operator + ") requires a Collection type, but got " + resolved.getClass().getSimpleName(),
                null
            );
        }

        return switch (operator) {
            case ANY -> collection.stream().anyMatch(item -> matches(item, predicate));
            case ALL -> collection.stream().allMatch(item -> matches(item, predicate));
            case NONE -> collection.stream().noneMatch(item -> matches(item, predicate));
        };
    }

    private Object resolveValue(Object candidate, ValueNode valueNode) {
        if (valueNode instanceof LiteralNode(Object literal)) {
            return literal;
        }
        if (valueNode instanceof ParameterNode(String name)) {
            if (!bindings.containsKey(name)) {
                throw new MissingParameterException(
                    "Parameter binding not found: '" + name + "'. " +
                    "Use expression.setParameter(\"" + name + "\", value) before evaluating."
                );
            }
            return bindings.get(name);
        }
        if (valueNode instanceof PropertyValueNode(List<String> propertyPath)) {
            Object right = candidate;
            for (String step : propertyPath) {
                if (right == null) break;
                right = reflect(right, step);
            }
            return right;
        }
        throw new EvaluatorException("Unsupported value node type: " + valueNode.getClass().getSimpleName(), null);
    }
}
