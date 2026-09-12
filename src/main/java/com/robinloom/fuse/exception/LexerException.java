package com.robinloom.fuse.exception;

/**
 * Thrown when a FUSE expression string contains an invalid or unrecognized
 * token during tokenization, before parsing begins.
 */
public class LexerException extends RuntimeException {

    /**
     * Constructs a new lexer exception.
     *
     * @param message a description of the invalid token, including its position
     */
    public LexerException(String message) {
        super(message);
    }
}
