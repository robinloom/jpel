package com.robinloom.fuse.parser.ast;

import java.util.List;

public record PathNode(List<SegmentNode> segments) implements ASTNode {}
