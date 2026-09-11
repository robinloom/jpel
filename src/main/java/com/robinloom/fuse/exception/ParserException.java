package com.robinloom.fuse.exception;

public class ParserException extends RuntimeException {

    public ParserException(String message, int position) {
        super(message + " (position: " + position + ")");
    }
}
