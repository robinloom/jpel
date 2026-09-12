package com.robinloom.fuse.exception;

/**
 * Thrown when a FUSE expression violates the grammar, e.g. unbalanced
 * brackets, a misplaced operator, or an incomplete pipe operation.
 */
public class ParserException extends RuntimeException {

    /**
     * Constructs a new parser exception.
     *
     * @param message a description of the grammar violation
     * @param position the character offset in the expression where the violation was detected
     */
    public ParserException(String message, int position) {
        super(message + " (position: " + position + ")");
    }
}
