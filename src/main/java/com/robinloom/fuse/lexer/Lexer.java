package com.robinloom.fuse.lexer;

import com.robinloom.fuse.exception.LexerException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {

    private static final Map<String, TokenType> KEYWORD_OPERATORS;

    static {
        Map<String, TokenType> map = new HashMap<>();
        map.put("contains", TokenType.CONTAINS);
        map.put("startsWith", TokenType.STARTS_WITH);
        map.put("endsWith", TokenType.ENDS_WITH);
        map.put("matches", TokenType.MATCHES);
        map.put("in", TokenType.IN);
        map.put("not", TokenType.NOT);
        map.put("count", TokenType.COUNT);
        map.put("countDistinct", TokenType.COUNT_DISTINCT);
        map.put("sum", TokenType.SUM);
        map.put("avg", TokenType.AVG);
        map.put("min", TokenType.MIN);
        map.put("max", TokenType.MAX);
        map.put("sort", TokenType.SORT);
        map.put("asc", TokenType.ASC);
        map.put("desc", TokenType.DESC);
        map.put("distinct", TokenType.DISTINCT);
        KEYWORD_OPERATORS = Map.copyOf(map);
    }

    private final List<Token> tokens = new ArrayList<>();
    private final String expression;

    private int cursor;

    public Lexer(String expression) {
        this.expression = expression;
    }

    public List<Token> tokenize() {
        while (!isEof()) {
            keepOnTokenizing();
        }

        tokens.add(new Token(TokenType.EOF, "EOF", expression.length()));
        return tokens;
    }

    private void keepOnTokenizing() {
        char c = peek();

        switch (c) {
            case '.':
                addMetaChar(TokenType.DOT);
                break;
            case '[':
                addMetaChar(TokenType.LBRACKET);
                break;
            case ']':
                addMetaChar(TokenType.RBRACKET);
                break;
            case '*':
                addMetaChar(TokenType.ASTERISK);
                break;
            case '+':
                addMetaChar(TokenType.PLUS);
                break;
            case '-':
                addMinusOrNumber();
                break;
            case '/':
                addMetaChar(TokenType.SLASH);
                break;
            case '"':
                addString();
                break;
            case '>':
                addGt();
                break;
            case '<':
                addLt();
                break;
            case '=':
                addEq();
                break;
            case '!':
                addNeq();
                break;
            case '&':
                addAnd();
                break;
            case '|':
                addOr();
                break;
            case '(':
                addMetaChar(TokenType.LPAREN);
                break;
            case ')':
                addMetaChar(TokenType.RPAREN);
                break;
            case ',':
                addMetaChar(TokenType.COMMA);
                break;
            case ':':
                addParameter();
                break;
            default:
                if (Character.isWhitespace(c)) {
                    advance();
                } else if (isBoolean()) {
                    addBoolean();
                } else if (isNull()) {
                    addNull();
                } else if (Character.isJavaIdentifierStart(c)) {
                    addIdentifier();
                } else if (Character.isDigit(c)) {
                    addNumber();
                } else {
                    throw new LexerException("Unexpected token: " + c + " at position " + cursor);
                }
        }
    }

    private boolean isEof() {
        return cursor >= expression.length();
    }

    private char peek() {
        return expression.charAt(cursor);
    }

    private char peekNext() {
        return cursor + 1 < expression.length()
               ? expression.charAt(cursor + 1)
               : '\0';
    }

    private void advance() {
        cursor++;
    }

    private void addMetaChar(TokenType tokenType) {
        tokens.add(new Token(tokenType, tokenType.name(), cursor));
        advance();
    }

    private void addString() {
        advance();
        int startPosition = cursor;
        while (!isEof() && peek() != '"') {
            advance();
        }
        advance();
        tokens.add(new Token(TokenType.STRING, expression.substring(startPosition, cursor-1), startPosition));
    }

    private void addIdentifier() {
        int startPosition = cursor;
        while (!isEof() && Character.isJavaIdentifierPart(peek())) {
            advance();
        }
        String value = expression.substring(startPosition, cursor);
        TokenType type = KEYWORD_OPERATORS.getOrDefault(value, TokenType.IDENTIFIER);
        tokens.add(new Token(type, value, startPosition));
    }

    private void addNumber() {
        int startPosition = cursor;
        if (peek() == '-') {
            advance();
        }
        while (!isEof() && (Character.isDigit(peek()) || peek() == '.')) {
            advance();
        }
        tokens.add(new Token(TokenType.NUMBER, expression.substring(startPosition, cursor), startPosition));
    }

    private void addMinusOrNumber() {
        if (isPrecededByValue()) {
            addMetaChar(TokenType.MINUS);
        } else {
            addNumber();
        }
    }

    private boolean isPrecededByValue() {
        if (tokens.isEmpty()) {
            return false;
        }
        TokenType last = tokens.getLast().type();
        return last == TokenType.IDENTIFIER || last == TokenType.NUMBER || last == TokenType.STRING
            || last == TokenType.BOOLEAN || last == TokenType.NULL
            || last == TokenType.RPAREN || last == TokenType.RBRACKET;
    }

    private void addGt() {
        if (peekNext() == '=') {
            advance();
            advance();
            tokens.add(new Token(TokenType.GTE, ">=", cursor));
        } else {
            tokens.add(new Token(TokenType.GT, ">", cursor));
            advance();
        }
    }

    private void addLt() {
        if (peekNext() == '=') {
            advance();
            advance();
            tokens.add(new Token(TokenType.LTE, "<=", cursor));
        } else {
            tokens.add(new Token(TokenType.LT, "<", cursor));
            advance();
        }
    }

    private void addEq() {
        if (peekNext() == '=') {
            tokens.add(new Token(TokenType.EQEQ, "==", cursor));
            advance();
            advance();
        } else {
            throw new LexerException("Unexpected token: " + peekNext() + " at position " + cursor);
        }
    }

    private void addNeq() {
        if (peekNext() == '=') {
            tokens.add(new Token(TokenType.EXCLAM_EQ, "!=", cursor));
            advance();
            advance();
        } else {
            tokens.add(new Token(TokenType.EXCLAM, "!", cursor));
            advance();
        }
    }

    private boolean isBoolean() {
        return expression.startsWith("true", cursor) || expression.startsWith("false", cursor);
    }

    private void addBoolean() {
        int length;
        if (expression.startsWith("true", cursor)) {
            tokens.add(new Token(TokenType.BOOLEAN, expression.substring(cursor, cursor + 4), cursor));
            length = 4;
        } else {
            tokens.add(new Token(TokenType.BOOLEAN, expression.substring(cursor, cursor + 5), cursor));
            length = 5;
        }
        for (int i = 0; i < length; i++) {
            advance();
        }
    }

    private boolean isNull() {
        return expression.startsWith("null", cursor);
    }

    private void addNull() {
        tokens.add(new Token(TokenType.NULL, "null", cursor));
        for (int i = 0; i < 4; i++) {
            advance();
        }
    }

    private void addAnd() {
        if (peekNext() == '&') {
            tokens.add(new Token(TokenType.AND_AND, "&&", cursor));
            advance();
            advance();
        } else {
            throw new LexerException("Unexpected token: " + peekNext() + " at position " + cursor);
        }
    }

    private void addOr() {
        if (peekNext() == '|') {
            tokens.add(new Token(TokenType.PIPE_PIPE, "||", cursor));
            advance();
            advance();
        } else {
            tokens.add(new Token(TokenType.PIPE, "|", cursor));
            advance();
        }
    }

    private void addParameter() {
        advance(); // skip ':'
        int startPosition = cursor;
        while (!isEof() && Character.isJavaIdentifierPart(peek())) {
            advance();
        }
        if (cursor == startPosition) {
            throw new LexerException("Expected parameter name after ':' at position " + startPosition);
        }
        tokens.add(new Token(TokenType.PARAMETER, expression.substring(startPosition, cursor), startPosition));
    }
}
