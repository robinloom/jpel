package com.robinloom.fuse;

import com.robinloom.fuse.lexer.Lexer;
import com.robinloom.fuse.lexer.Token;
import com.robinloom.fuse.parser.Parser;
import com.robinloom.fuse.parser.ast.ASTNode;

import java.util.List;

/**
 * Entry point for the FUSE query language: a lightweight, type-safe way to
 * navigate, filter, sort, and aggregate Java object graphs using string
 * expressions.
 *
 * <pre>{@code
 * Object result = FUSE.eval("person.address.city", person);
 *
 * List<Person> adults = FUSE.compile("persons[age >= 18]")
 *     .getResultList(data, Person.class);
 * }</pre>
 *
 * @see Expression
 */
public final class FUSE {

    private FUSE() {}

    /**
     * Parses {@code expression} into a reusable {@link Expression}. Compile
     * once and evaluate repeatedly against different objects, or reuse a
     * compiled expression with different {@link Expression#setParameter
     * parameter} values.
     *
     * @param expression a FUSE expression, e.g. {@code "persons[age >= :minAge]"}
     * @return the compiled, evaluable expression
     * @throws com.robinloom.fuse.exception.LexerException if {@code expression} contains an invalid token
     * @throws com.robinloom.fuse.exception.ParserException if {@code expression} violates the FUSE grammar
     */
    public static Expression compile(String expression) {
        List<Token> tokens = new Lexer(expression).tokenize();

        Parser parser = new Parser(tokens);
        ASTNode ast = parser.parse();

        return new Expression(ast);
    }

    /**
     * Compiles and immediately evaluates {@code expression} against
     * {@code object} in one step. Equivalent to
     * {@code compile(expression).eval(object)}; prefer {@link #compile}
     * directly when the same expression is evaluated more than once.
     *
     * @param expression a FUSE expression, e.g. {@code "orders[items.any(price > 100)]"}
     * @param object the root object to evaluate the expression against
     * @return the evaluation result; may be a scalar, a {@link List}, or {@code null}
     * @throws com.robinloom.fuse.exception.LexerException if {@code expression} contains an invalid token
     * @throws com.robinloom.fuse.exception.ParserException if {@code expression} violates the FUSE grammar
     * @throws com.robinloom.fuse.exception.EvaluatorException if evaluation fails, e.g. due to null navigation or a type mismatch
     */
    public static Object eval(String expression, Object object) {
        return compile(expression).eval(object);
    }
}
