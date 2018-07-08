/*
 * Copyright 2015 Stuart Scott
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package frontend;

import expression.ExpressionList;
import expression.IExpression;
import expression.Literal;
import expression.function.Function;
import expression.function.IFunction;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import match.IMatch;
import match.ITarget;
import match.Target;

public class Parser implements IParser {

    private ILexer lexer;
    private IMatch match;
    private File file;
    private ITarget target;

    /**
     * Creates a parser with the given Lexer.
     */
    public Parser(IMatch match, ILexer lexer) {
        this.match = match;
        this.lexer = lexer;
        file = lexer.getFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ITarget> parse() {
        List<ITarget> targets = new ArrayList<ITarget>();
        lexer.move();
        while (!lexer.currentIs(Category.EOF)) {
            target = new Target(match, file);
            target.setFunction(matchFunction());
            targets.add(target);
        }
        return targets;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IFunction matchFunction() {
        String name = lexer.match(Category.UPPER_CASE);
        return Function.getFunction(name, match, target, matchParameters());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, IExpression> matchParameters() {
        Map<String, IExpression> expressions = new HashMap<String, IExpression>();
        lexer.match(Category.ORB);
        while (!lexer.currentIs(Category.CRB)) {
            if (lexer.currentIs(Category.LOWER_CASE)) {
                String name = lexer.match(Category.LOWER_CASE);
                lexer.match(Category.ASSIGN);
                expressions.put(name, matchExpression());
            } else {
                // Single parameters can be anonymous
                expressions.put(Function.ANONYMOUS, matchExpression());
                break;
            }
        }
        lexer.match(Category.CRB);
        return expressions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IExpression matchExpression() {
        switch (lexer.getCurrentCategory()) {
            case UPPER_CASE:
                return matchFunction();
            case OSB:
                List<IExpression> expressions = new ArrayList<IExpression>();
                lexer.move();
                while (!lexer.currentIs(Category.CSB)) {
                    expressions.add(matchExpression());
                }
                lexer.move();
                return new ExpressionList(match, target, expressions);
            default:
                String literal = lexer.match(Category.STRING_LITERAL);
                return new Literal(match, target, literal.substring(1, literal.length() - 1));
        }
    }
}
