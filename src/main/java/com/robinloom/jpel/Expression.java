package com.robinloom.jpel;

import com.robinloom.jpath.evaluator.Evaluator;
import com.robinloom.jpath.exception.NonUniqueResultException;
import com.robinloom.jpath.parser.ast.PathNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Expression {

    private final PathNode ast;
    private final Map<String, Object> bindings = new HashMap<>();

    public Expression(PathNode ast) {
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

    public Object eval(Object object) {
        return new Evaluator(object, ast, bindings).eval();
    }
}
