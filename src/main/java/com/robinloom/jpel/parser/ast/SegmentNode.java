package com.robinloom.jpel.parser.ast;

public record SegmentNode(String name, Integer index, FilterNode filter) implements ASTNode {
}
