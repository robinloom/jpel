package com.robinloom.fuse.parser.ast;

public record NegationNode(LogicalNode inner) implements LogicalNode {}
