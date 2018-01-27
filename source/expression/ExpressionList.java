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
package expression;

import main.IMatch;
import main.ITarget;
import main.Utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExpressionList extends Expression {

    private List<IExpression> mElements;

    public ExpressionList(IMatch match, ITarget target, IExpression element) {
        this(match, target, Collections.singletonList(element));
    }

    public ExpressionList(IMatch match, ITarget target, List<IExpression> elements) {
        super(match, target);
        mElements = elements;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        for (IExpression element : mElements) {
            element.configure();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        return Utilities.join(" ", resolveList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> resolveList() {
        List<String> values = new ArrayList<String>();
        for (IExpression element : mElements) {
            values.addAll(element.resolveList());
        }
        return values;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return resolveList().toString();
    }
}
