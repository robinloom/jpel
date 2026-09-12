package com.robinloom.fuse.exception;

/**
 * Thrown when a compiled expression references a named parameter (e.g.
 * {@code :minAge}) that was never bound via
 * {@link com.robinloom.fuse.Expression#setParameter(String, Object)} before
 * evaluation.
 */
public class MissingParameterException extends RuntimeException {

    /**
     * Constructs a new missing-parameter exception.
     *
     * @param message a description naming the missing parameter
     */
    public MissingParameterException(String message) {
        super(message);
    }
}
