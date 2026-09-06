package com.robinloom.jpel.parser.ast;

import com.robinloom.jpel.parser.LogicalOperator;

public record BinaryLogicalNode(LogicalNode left, LogicalOperator logicalOperator, LogicalNode right) implements LogicalNode {
}
