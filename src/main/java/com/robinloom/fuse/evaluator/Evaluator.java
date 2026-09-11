package com.robinloom.fuse.evaluator;

import com.robinloom.fuse.parser.ast.FilterNode;
import com.robinloom.fuse.parser.ast.PathNode;
import com.robinloom.fuse.parser.ast.SegmentNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public final class Evaluator {

    private final Object root;
    private final PathNode ast;
    private final FilterEvaluator filterEvaluator;

    public Evaluator(Object root, PathNode ast, Map<String, Object> bindings) {
        this.root = root;
        this.ast = ast;
        this.filterEvaluator = new FilterEvaluator(bindings);
    }

    public Object eval() {
        Object current = root;

        for (SegmentNode segment : ast.segments()) {
            current = resolve(current, segment);
        }

        return current;
    }

    private Object resolve(Object object, SegmentNode segment) {
        if (object == null) {
            return null;
        }

        if (object instanceof Collection<?> c) {
            return flatMap(c, segment);
        } else {
            return map(object, segment);
        }
    }

    private Object flatMap(Collection<?> c, SegmentNode segmentNode) {

        Object result = c.stream()
                         .filter(Objects::nonNull)
                         .map(o -> reflect(o, segmentNode.name()))
                         .flatMap(result1 -> {

                    if (result1 instanceof Collection<?> collection) {
                        return collection.stream();
                    }

                    return Stream.of(result1);
                })
                         .toList();

        if (segmentNode.filter() != null) {
            result = applyFilter(result, segmentNode.filter());
        }

        if (result instanceof Collection<?> col && segmentNode.index() != null) {
            return getObjectByIndex(new ArrayList<>(col), segmentNode.index());
        }

        return result;
    }

    private Object map(Object object, SegmentNode segment) {
        if (object == null) {
            return null;
        }

        Object result = reflect(object, segment.name());

        if (segment.filter() != null) {
            result = applyFilter(result, segment.filter());
        }

        if (result instanceof Collection<?> c && segment.index() != null) {
            return getObjectByIndex(new ArrayList<>(c), segment.index());
        }

        return result;
    }

    private <E> E getObjectByIndex(ArrayList<E> list, int index) {
        if (index < 0) {
            return list.get(list.size() + index);
        } else {
            return list.get(index);
        }
    }

    private Object applyFilter(Object result, FilterNode filter) {
        if (!(result instanceof Collection<?> collection)) {
            return result;
        }

        return collection.stream()
                         .filter(item -> filterEvaluator.matches(item, filter.expression()))
                         .toList();
    }

    private Object reflect(Object object, String property) {
        return PropertyResolver.resolve(object, property);
    }
}
