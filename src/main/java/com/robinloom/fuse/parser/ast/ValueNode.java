package com.robinloom.fuse.parser.ast;

sealed public interface ValueNode extends ASTNode permits LiteralNode, ParameterNode, PropertyValueNode, ArithmeticNode {}
