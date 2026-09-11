package com.robinloom.fuse.parser.ast;

import com.robinloom.fuse.parser.CollectionOperator;

import java.util.List;

public record CollectionConditionNode(List<String> propertyPath, CollectionOperator operator, LogicalNode predicate) implements LogicalNode {}
