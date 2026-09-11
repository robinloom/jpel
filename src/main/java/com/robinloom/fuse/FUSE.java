package com.robinloom.fuse;

import com.robinloom.fuse.lexer.Lexer;
import com.robinloom.fuse.lexer.Token;
import com.robinloom.fuse.parser.Parser;
import com.robinloom.fuse.parser.ast.ASTNode;

import java.util.List;

public final class FUSE {

    private FUSE() {}

    public static Expression compile(String expression) {
        List<Token> tokens = new Lexer(expression).tokenize();

        Parser parser = new Parser(tokens);
        ASTNode ast = parser.parse();

        return new Expression(ast);
    }

    public static Object eval(String expression, Object object) {
        return compile(expression).eval(object);
    }
}
