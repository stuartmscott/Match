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
import main.IMatch;
import main.ITarget;
import main.Match;
import main.Target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser implements IParser {

    private ILexer mLexer;
    private IMatch mMatch;
    private ITarget mTarget;

    public Parser(IMatch match, ILexer lexer) {
        mMatch = match;
        mLexer = lexer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ITarget> parse() {
        List<ITarget> targets = new ArrayList<ITarget>();
        mLexer.move();
        while (!mLexer.currentIs(Category.EOF)) {
            mTarget = new Target(mMatch);
            mTarget.setFunction(matchFunction());
            targets.add(mTarget);
        }
        return targets;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IFunction matchFunction() {
        String name = mLexer.match(Category.IDENTIFIER);
        return Function.getFunction(name, mMatch, mTarget, matchParameters());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, IExpression> matchParameters() {
        Map<String, IExpression> expressions = new HashMap<String, IExpression>();
        mLexer.match(Category.ORB);
        while (!mLexer.currentIs(Category.CRB)) {
            if (mLexer.currentIs(Category.IDENTIFIER)) {
                String name = mLexer.match(Category.IDENTIFIER);
                mLexer.match(Category.ASSIGN);
                expressions.put(name, matchExpression());
            } else {
                // Single parameters can be anonymous
                expressions.put(Function.ANONYMOUS, matchExpression());
                break;
            }
        }
        mLexer.match(Category.CRB);
        return expressions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IExpression matchExpression() {
        switch (mLexer.getCurrentCategory()) {
            case IDENTIFIER:
                return matchFunction();
            case OSB:
                List<IExpression> expressions = new ArrayList<IExpression>();
                mLexer.move();
                while (!mLexer.currentIs(Category.CSB)) {
                    expressions.add(matchExpression());
                }
                mLexer.move();
                return new ExpressionList(mMatch, mTarget, expressions);
            default:
                String literal = mLexer.match(Category.STRING_LITERAL);
                return new Literal(mMatch, mTarget, literal.substring(1, literal.length() - 1));
        }
    }
}
