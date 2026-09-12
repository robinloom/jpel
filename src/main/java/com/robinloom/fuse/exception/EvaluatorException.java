package com.robinloom.fuse.exception;

/**
 * Thrown when evaluating a compiled expression against a concrete object
 * fails, e.g. due to null navigation on a required path segment, a property
 * that cannot be resolved via getter/field access, or a type mismatch during
 * comparison.
 */
public class EvaluatorException extends RuntimeException {

    /**
     * Constructs a new evaluator exception.
     *
     * @param message a description of the evaluation failure
     * @param cause the underlying cause, if any
     */
    public EvaluatorException(String message, Throwable cause) {
        super(message, cause);
    }
}
