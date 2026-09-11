package com.robinloom.fuse.parser.ast;

sealed public interface LogicalNode extends ASTNode permits BinaryLogicalNode, ConditionNode, NegationNode, CollectionConditionNode {}
