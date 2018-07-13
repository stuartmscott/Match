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

/**
 * A fake function for testing.
 */
public class FunctionFake extends Function {

    private String resolution = "foobar";

    /**
     * Initializes the function with the given parameters.
     */
    public FunctionFake(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        if (parameters.containsKey(ANONYMOUS)) {
            resolution = getParameter(ANONYMOUS).resolve();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        return resolution;
    }
}
