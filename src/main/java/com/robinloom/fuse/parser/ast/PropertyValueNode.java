package com.robinloom.fuse.parser.ast;

import java.util.List;

public record PropertyValueNode(List<String> propertyPath) implements ValueNode {}
