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

import java.util.List;

public class ExpressionList extends Expression {

    public static final String SEPARATOR = ";";

    private List<IExpression> mElements;

    public ExpressionList(IMatch match, ITarget target, List<IExpression> elements) {
        super(match, target);
        mElements = elements;
    }

    public String resolve() {
        StringBuilder values = new StringBuilder();
        boolean first = true;
        for (IExpression element : mElements) {
            if (first) {
                first = false;
            } else {
                values.append(SEPARATOR);
            }
            values.append(element.resolve());
        }
        return values.toString();
    }
}