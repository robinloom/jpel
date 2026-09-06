package com.robinloom.jpel.parser.ast;

sealed public interface LogicalNode extends ASTNode permits BinaryLogicalNode, ConditionNode, NegationNode, CollectionConditionNode {}
