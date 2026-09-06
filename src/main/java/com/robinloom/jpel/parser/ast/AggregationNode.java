package com.robinloom.jpel.parser.ast;

import java.util.Optional;

public record AggregationNode(
    ASTNode source,
    AggregationType type,
    Optional<String> property
) implements ASTNode {}
