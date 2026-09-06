package com.robinloom.jpel.parser.ast;

import com.robinloom.jpel.parser.ComparisonOperator;

import java.util.List;

public record ConditionNode(List<String> propertyPath, ComparisonOperator comparisonOperator, ValueNode right) implements LogicalNode {}
