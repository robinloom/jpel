package com.robinloom.fuse.parser.ast;

import com.robinloom.fuse.parser.ComparisonOperator;

public record ConditionNode(ValueNode left, ComparisonOperator comparisonOperator, ValueNode right) implements LogicalNode {}
