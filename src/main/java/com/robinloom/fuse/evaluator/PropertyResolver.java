package com.robinloom.fuse.evaluator;

import com.robinloom.fuse.exception.EvaluatorException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public final class PropertyResolver {

    private static final Object PROPERTY_NOT_FOUND = new Object();

    public static Object resolve(Object object, String property) {
        if (object == null) {
            return null;
        }

        Class<?> clazz = object.getClass();

        Object result = tryInvokeMethod(object, clazz, property);
        if (result == PROPERTY_NOT_FOUND) {
            result = tryAccessField(object, clazz, property);
        }

        if (result == PROPERTY_NOT_FOUND) {
            throw new EvaluatorException(
                    "Cannot resolve property '" + property + "' on type " + clazz.getSimpleName(),
                    new NoSuchMethodException());
        }

        return unwrapOptional(result);
    }

    private static Object unwrapOptional(Object value) {
        if (value instanceof Optional<?> optional) {
            return optional.orElse(null);
        }
        if (value instanceof OptionalInt optional) {
            return optional.isPresent() ? optional.getAsInt() : null;
        }
        if (value instanceof OptionalLong optional) {
            return optional.isPresent() ? optional.getAsLong() : null;
        }
        if (value instanceof OptionalDouble optional) {
            return optional.isPresent() ? optional.getAsDouble() : null;
        }
        return value;
    }

    private static Object tryInvokeMethod(Object object, Class<?> clazz, String property) {
        try {
            // Try exact name (Records)
            try {
                Method method = clazz.getMethod(property);
                return method.invoke(object);
            } catch (NoSuchMethodException _) {
            }

            // Try JavaBean getter convention (getName)
            String getterName = "get" + capitalize(property);
            try {
                Method method = clazz.getMethod(getterName);
                return method.invoke(object);
            } catch (NoSuchMethodException _) {
            }

            // Try is/has convention for booleans
            String booleanGetterName = "is" + capitalize(property);
            try {
                Method method = clazz.getMethod(booleanGetterName);
                return method.invoke(object);
            } catch (NoSuchMethodException _) {
            }

            String hasGetterName = "has" + capitalize(property);
            try {
                Method method = clazz.getMethod(hasGetterName);
                return method.invoke(object);
            } catch (NoSuchMethodException _) {
            }

            return PROPERTY_NOT_FOUND;
        } catch (ReflectiveOperationException e) {
            throw new EvaluatorException("Error invoking property '" + property + "'", e);
        }
    }

    private static Object tryAccessField(Object object, Class<?> clazz, String property) {
        try {
            Field field = clazz.getField(property);
            return field.get(object);
        } catch (NoSuchFieldException _) {
            return PROPERTY_NOT_FOUND;
        } catch (ReflectiveOperationException e) {
            throw new EvaluatorException("Error accessing field '" + property + "'", e);
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
