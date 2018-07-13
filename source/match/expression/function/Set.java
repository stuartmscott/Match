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

package match.expression.function;

import java.util.Map;

import match.IMatch;
import match.ITarget;
import match.expression.IExpression;
import match.expression.Literal;

/**
 * Sets the key/value pair as a property in the build.
 */
public class Set extends Function {

    protected String key;
    protected String value;

    /**
     * Initializes the function with the given parameters.
     */
    public Set(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        IExpression k = getParameter(NAME);
        IExpression v = getParameter(VALUE);
        if (!(k instanceof Literal)) {
            match.error("Set function expects a String key");
        }
        if (!(v instanceof Literal)) {
            match.error("Set function expects a String value");
        }
        key = k.resolve();
        value = v.resolve();
        target.setName("Set:" + key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        match.setProperty(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        return value;
    }
}
