package com.robinloom.jpel.parser.ast;

import com.robinloom.jpel.parser.CollectionOperator;

import java.util.List;

public record CollectionConditionNode(List<String> propertyPath, CollectionOperator operator, LogicalNode predicate) implements LogicalNode {}
