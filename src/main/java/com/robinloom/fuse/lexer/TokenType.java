package com.robinloom.fuse.lexer;

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

    PLUS,
    MINUS,
    SLASH,

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

}
