package com.robinloom.fuse.parser.ast;

import java.util.Optional;

public record DistinctNode(
    ASTNode source,
    Optional<String> property
) implements ASTNode {}
