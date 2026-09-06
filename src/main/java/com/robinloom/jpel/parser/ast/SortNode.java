package com.robinloom.jpel.parser.ast;

public record SortNode(
    ASTNode source,
    String property,
    SortDirection direction
) implements ASTNode {}
