package com.robinloom.fuse.lexer;

import java.util.Set;

public enum TokenType {
    IDENTIFIER,
    NUMBER,
    STRING,
    BOOLEAN,
    NULL,
    DOT,
    LBRACKET,
    RBRACKET,
    ASTERISK,

    EQEQ,
    EXCLAM_EQ,
    GT,
    LT,
    GTE,
    LTE,

    CONTAINS,
    STARTS_WITH,
    ENDS_WITH,
    MATCHES,

    IN,
    NOT,
    COMMA,

    AND_AND,
    PIPE_PIPE,
    EXCLAM,

    LPAREN,
    RPAREN,

    PARAMETER,

    PIPE,
    COUNT,
    COUNT_DISTINCT,
    SUM,
    AVG,
    MIN,
    MAX,
    SORT,
    ASC,
    DESC,
    DISTINCT,

    EOF;

    public static final Set<TokenType> LITERAL_TYPES = Set.of(
            TokenType.STRING, TokenType.NUMBER, TokenType.BOOLEAN, TokenType.NULL);
}
