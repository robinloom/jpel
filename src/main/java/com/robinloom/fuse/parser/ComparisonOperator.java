package com.robinloom.fuse.parser;

import com.robinloom.fuse.lexer.TokenType;

public enum ComparisonOperator {
    EQ,
    NEQ,
    GT,
    LT,
    GTE,
    LTE,

    CONTAINS,
    STARTS_WITH,
    ENDS_WITH,
    MATCHES,

    IN,
    NOT_IN;

    public static ComparisonOperator fromToken(TokenType tokenType) {
        return switch (tokenType) {
            case EQEQ -> EQ;
            case EXCLAM_EQ -> NEQ;
            case GT -> GT;
            case LT -> LT;
            case GTE -> GTE;
            case LTE -> LTE;
            case CONTAINS -> CONTAINS;
            case STARTS_WITH -> STARTS_WITH;
            case ENDS_WITH -> ENDS_WITH;
            case MATCHES -> MATCHES;
            default -> null;
        };
    }
}
