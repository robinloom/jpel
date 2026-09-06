package com.robinloom.jpel.parser.ast;

import java.util.List;

public record PathNode(List<SegmentNode> segments) implements ASTNode {}
