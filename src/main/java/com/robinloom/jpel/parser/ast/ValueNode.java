package com.robinloom.jpel.parser.ast;

sealed public interface ValueNode extends ASTNode permits LiteralNode, ParameterNode {}
