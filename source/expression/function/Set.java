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
package expression.function;

import expression.IExpression;
import expression.Literal;
import main.IMatch;
import main.ITarget;
import java.util.Map;

public class Set extends Function {

    protected String mKey;
    protected String mValue;

    public Set(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        IExpression key = getParameter(NAME);
        IExpression value = getParameter(VALUE);
        if (!(key instanceof Literal)) {
            mMatch.error("Set function expects a String key");
        }
        if (!(value instanceof Literal)) {
            mMatch.error("Set function expects a String value");
        }
        mKey = key.resolve();
        mValue = value.resolve();
        target.setName("Set:" + mKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        mMatch.setProperty(mKey, mValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        return mValue;
    }
}
