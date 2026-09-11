package com.robinloom.fuse.parser.ast;

import com.robinloom.fuse.parser.LogicalOperator;

public record BinaryLogicalNode(LogicalNode left, LogicalOperator logicalOperator, LogicalNode right) implements LogicalNode {
}
