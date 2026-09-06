package com.robinloom.jpel;

import com.robinloom.jpath.lexer.Lexer;
import com.robinloom.jpath.lexer.Token;
import com.robinloom.jpath.parser.Parser;
import com.robinloom.jpath.parser.ast.PathNode;

import java.util.List;

public final class JPath {

    private JPath() {}

    public static Expression compile(String expression) {
        List<Token> tokens = new Lexer(expression).tokenize();

        Parser parser = new Parser(tokens);
        PathNode ast = parser.parse();

        return new Expression(ast);
    }

    public static Object eval(String expression, Object object) {
        return compile(expression).eval(object);
    }
}
