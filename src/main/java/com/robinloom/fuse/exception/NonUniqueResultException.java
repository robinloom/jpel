package com.robinloom.fuse.exception;

/**
 * Thrown by {@link com.robinloom.fuse.Expression#getSingleResult(Object, Class)}
 * and {@link com.robinloom.fuse.Expression#getOptionalResult(Object, Class)}
 * when the expression matches more than one element instead of at most one.
 */
public class NonUniqueResultException extends RuntimeException {

    /**
     * Constructs a new non-unique-result exception.
     *
     * @param message a description including the number of matches found
     */
    public NonUniqueResultException(String message) {
        super(message);
    }
}
