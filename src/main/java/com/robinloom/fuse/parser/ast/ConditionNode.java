package com.robinloom.fuse.parser.ast;

import com.robinloom.fuse.parser.ComparisonOperator;

import java.util.List;

public record ConditionNode(List<String> propertyPath, ComparisonOperator comparisonOperator, ValueNode right) implements LogicalNode {}
