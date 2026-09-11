package com.robinloom.fuse.parser.ast;

import com.robinloom.fuse.parser.ArithmeticOperator;

public record ArithmeticNode(
    ValueNode left,
    ArithmeticOperator operator,
    ValueNode right
) implements ValueNode {}
