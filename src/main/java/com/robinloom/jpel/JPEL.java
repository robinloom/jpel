package com.robinloom.jpel;

import com.robinloom.jpel.lexer.Lexer;
import com.robinloom.jpel.lexer.Token;
import com.robinloom.jpel.parser.Parser;
import com.robinloom.jpel.parser.ast.PathNode;

import java.util.List;

public final class JPEL {

    private JPEL() {}

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
