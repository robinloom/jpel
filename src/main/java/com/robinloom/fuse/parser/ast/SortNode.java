package com.robinloom.fuse.parser.ast;

public record SortNode(
    ASTNode source,
    String property,
    SortDirection direction
) implements ASTNode {}
