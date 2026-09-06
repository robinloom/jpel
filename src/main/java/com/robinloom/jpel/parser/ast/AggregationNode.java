package com.robinloom.jpel.parser.ast;

import java.util.Optional;

public record AggregationNode(
    PathNode source,
    AggregationType type,
    Optional<String> property
) implements ASTNode {}
