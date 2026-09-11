package com.robinloom.fuse.parser;

import com.robinloom.fuse.exception.ParserException;
import com.robinloom.fuse.lexer.Token;
import com.robinloom.fuse.lexer.TokenType;
import com.robinloom.fuse.parser.ast.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class Parser {

    private final List<Token> tokens;
    private int cursor;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public ASTNode parse() {
        List<SegmentNode> segments = new ArrayList<>();

        do {
            segments.add(parseSegment());
        } while (match(TokenType.DOT));

        ASTNode result = new PathNode(segments);

        while (match(TokenType.PIPE)) {
            if (peek().type() == TokenType.SORT) {
                result = parseSort(result);
            } else if (peek().type() == TokenType.DISTINCT) {
                result = parseDistinct(result);
            } else if (isAggregationFunction(peek().type())) {
                result = parseAggregation(result);
                break;
            } else {
                throw new ParserException("Expected aggregation, sort or distinct function", cursor);
            }
        }

        consume(TokenType.EOF);
        return result;
    }

    private boolean isAggregationFunction(TokenType type) {
        return type == TokenType.COUNT || type == TokenType.COUNT_DISTINCT || type == TokenType.SUM
            || type == TokenType.AVG || type == TokenType.MIN || type == TokenType.MAX;
    }

    private SortNode parseSort(ASTNode source) {
        consume(TokenType.SORT);
        consume(TokenType.LPAREN);

        StringBuilder propBuilder = new StringBuilder();
        propBuilder.append(consume(TokenType.IDENTIFIER).value());

        while (match(TokenType.DOT)) {
            propBuilder.append(".").append(consume(TokenType.IDENTIFIER).value());
        }

        String property = propBuilder.toString();

        SortDirection direction = SortDirection.ASC;
        if (peek().type() == TokenType.ASC || peek().type() == TokenType.DESC) {
            direction = peek().type() == TokenType.ASC ? SortDirection.ASC : SortDirection.DESC;
            advance();
        }

        consume(TokenType.RPAREN);
        return new SortNode(source, property, direction);
    }

    private DistinctNode parseDistinct(ASTNode source) {
        consume(TokenType.DISTINCT);
        consume(TokenType.LPAREN);

        java.util.Optional<String> property = java.util.Optional.empty();
        if (peek().type() != TokenType.RPAREN) {
            StringBuilder propBuilder = new StringBuilder();
            propBuilder.append(consume(TokenType.IDENTIFIER).value());

            while (match(TokenType.DOT)) {
                propBuilder.append(".").append(consume(TokenType.IDENTIFIER).value());
            }

            property = java.util.Optional.of(propBuilder.toString());
        }

        consume(TokenType.RPAREN);
        return new DistinctNode(source, property);
    }

    private AggregationNode parseAggregation(ASTNode source) {
        Token token = peek();
        AggregationType type = switch (token.type()) {
            case COUNT -> AggregationType.COUNT;
            case COUNT_DISTINCT -> AggregationType.COUNT_DISTINCT;
            case SUM -> AggregationType.SUM;
            case AVG -> AggregationType.AVG;
            case MIN -> AggregationType.MIN;
            case MAX -> AggregationType.MAX;
            default -> throw new ParserException("Expected aggregation function", cursor);
        };
        advance();

        consume(TokenType.LPAREN);

        java.util.Optional<String> property = java.util.Optional.empty();
        if (type != AggregationType.COUNT && !peek().type().equals(TokenType.RPAREN)) {
            Token propToken = consume(TokenType.IDENTIFIER);
            property = java.util.Optional.of(propToken.value());
        }

        consume(TokenType.RPAREN);
        return new AggregationNode(source, type, property);
    }

    private Token peek() {
        return tokens.get(cursor);
    }

    private boolean isEof() {
        return peek().type() == TokenType.EOF;
    }

    private void advance() {
        if (!isEof()) {
            cursor++;
        }
    }

    private Token consume(TokenType... expected) {
        Token token = peek();

        if (!Set.of(expected).contains(token.type())) {
            throw new ParserException("Expected " + Arrays.toString(expected) + " but got " + token.type(), cursor);
        }

        advance();
        return token;
    }

    private boolean match(TokenType type) {
        if (peek().type() != type) {
            return false;
        }

        advance();
        return true;
    }

    private Token peekAt(int offset) {
        int index = Math.min(cursor + offset, tokens.size() - 1);
        return tokens.get(index);
    }

    private SegmentNode parseSegment() {
        Token identifier = consume(TokenType.IDENTIFIER);

        Integer index = null;
        FilterNode filter = null;

        if (match(TokenType.LBRACKET)) {

            if (peek().type() == TokenType.NUMBER && peekAt(1).type() == TokenType.RBRACKET) {
                index = consume(TokenType.NUMBER).intValue();
            } else {
                filter = parseFilter();
            }

            consume(TokenType.RBRACKET);
        }

        return new SegmentNode(identifier.value(), index, filter);
    }

    private FilterNode parseFilter() {
        return new FilterNode(parseLogicalExpression());
    }

    private LogicalNode parseLogicalExpression() {
        return parseOrExpression();
    }

    private LogicalNode parseOrExpression() {
        LogicalNode left = parseAndExpression();
        while (peek().type() == TokenType.PIPE_PIPE) {
            consume(TokenType.PIPE_PIPE);
            LogicalNode right = parseAndExpression();
            left = new BinaryLogicalNode(left, LogicalOperator.OR, right);
        }
        return left;
    }

    private LogicalNode parseAndExpression() {
        LogicalNode left = parsePrimary();
        while (peek().type() == TokenType.AND_AND) {
            consume(TokenType.AND_AND);
            LogicalNode right = parsePrimary();
            left = new BinaryLogicalNode(left, LogicalOperator.AND, right);
        }
        return left;
    }

    private LogicalNode parsePrimary() {
        if (match(TokenType.EXCLAM)) {
            return new NegationNode(parsePrimary());
        }
        if (match(TokenType.LPAREN)) {
            LogicalNode inner = parseLogicalExpression();
            consume(TokenType.RPAREN);
            return inner;
        }
        return parseCondition();
    }

    private LogicalNode parseCondition() {
        ValueNode left = parseArithmeticExpression();

        if (left instanceof PropertyValueNode(List<String> path) && !path.isEmpty()) {
            CollectionOperator collectionOperator = asCollectionOperator(path.getLast());
            if (collectionOperator != null && peek().type() == TokenType.LPAREN) {
                List<String> prefix = new ArrayList<>(path.subList(0, path.size() - 1));
                if (prefix.isEmpty()) {
                    throw new ParserException("Collection operator requires a preceding property path", cursor);
                }
                consume(TokenType.LPAREN);
                LogicalNode predicate = parseLogicalExpression();
                consume(TokenType.RPAREN);
                return new CollectionConditionNode(prefix, collectionOperator, predicate);
            }
        }

        if (match(TokenType.NOT)) {
            consume(TokenType.IN);
            return new ConditionNode(left, ComparisonOperator.NOT_IN, parseListValue());
        }
        if (match(TokenType.IN)) {
            return new ConditionNode(left, ComparisonOperator.IN, parseListValue());
        }

        Token operatorToken = consume(TokenType.EQEQ, TokenType.EXCLAM_EQ, TokenType.LT,
                                      TokenType.LTE, TokenType.GT, TokenType.GTE,
                                      TokenType.CONTAINS, TokenType.STARTS_WITH,
                                      TokenType.ENDS_WITH, TokenType.MATCHES);
        ComparisonOperator comparisonOperator = ComparisonOperator.fromToken(operatorToken.type());
        ValueNode right = parseArithmeticExpression();
        return new ConditionNode(left, comparisonOperator, right);
    }

    private ValueNode parseArithmeticExpression() {
        ValueNode left = parseTerm();
        while (peek().type() == TokenType.PLUS || peek().type() == TokenType.MINUS) {
            ArithmeticOperator operator = peek().type() == TokenType.PLUS
                ? ArithmeticOperator.ADD
                : ArithmeticOperator.SUBTRACT;
            advance();
            ValueNode right = parseTerm();
            left = new ArithmeticNode(left, operator, right);
        }
        return left;
    }

    private ValueNode parseTerm() {
        ValueNode left = parseFactor();
        while (peek().type() == TokenType.ASTERISK || peek().type() == TokenType.SLASH) {
            ArithmeticOperator operator = peek().type() == TokenType.ASTERISK
                ? ArithmeticOperator.MULTIPLY
                : ArithmeticOperator.DIVIDE;
            advance();
            ValueNode right = parseFactor();
            left = new ArithmeticNode(left, operator, right);
        }
        return left;
    }

    private ValueNode parseFactor() {
        if (peek().type() == TokenType.PARAMETER) {
            return parseParameter();
        }
        if (peek().type() == TokenType.IDENTIFIER) {
            return new PropertyValueNode(parsePropertyPath());
        }
        return parseLiteral();
    }

    private LiteralNode parseListLiteral() {
        consume(TokenType.LBRACKET);
        List<Object> values = new ArrayList<>();
        if (peek().type() != TokenType.RBRACKET) {
            values.add(parseLiteral().literal());
            while (match(TokenType.COMMA)) {
                values.add(parseLiteral().literal());
            }
        }
        consume(TokenType.RBRACKET);
        return new LiteralNode(values);
    }

    private List<String> parsePropertyPath() {
        List<String> path = new ArrayList<>();
        path.add(consume(TokenType.IDENTIFIER).value());
        while (peek().type() == TokenType.DOT) {
            consume(TokenType.DOT);
            path.add(consume(TokenType.IDENTIFIER).value());
        }
        return path;
    }

    private LiteralNode parseLiteral() {
        Token token = consume(TokenType.STRING, TokenType.NUMBER, TokenType.BOOLEAN, TokenType.NULL);
        Object value = switch (token.type()) {
            case STRING -> token.value();
            case NUMBER -> {
                String raw = token.value();
                if (raw.contains(".")) yield token.doubleValue();
                try { yield Integer.parseInt(raw); }
                catch (NumberFormatException e) { yield token.longValue(); }
            }
            case BOOLEAN -> token.booleanValue();
            case NULL -> null;
            default -> throw new ParserException("Unexpected token type: " + token.type(), cursor);
        };
        return new LiteralNode(value);
    }

    private CollectionOperator asCollectionOperator(String identifier) {
        return switch (identifier) {
            case "any" -> CollectionOperator.ANY;
            case "all" -> CollectionOperator.ALL;
            case "none" -> CollectionOperator.NONE;
            default -> null;
        };
    }

    private ValueNode parseListValue() {
        if (peek().type() == TokenType.PARAMETER) {
            return parseParameter();
        }
        return parseListLiteral();
    }

    private ParameterNode parseParameter() {
        Token token = consume(TokenType.PARAMETER);
        return new ParameterNode(token.value());
    }

}
