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
import main.IMatch;
import main.ITarget;
import main.Match;

import java.util.Map;

public class Set extends Function {

    static {
        register(Set.class, "set");
    }

    private String mKey;

    public Set(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        if (parameters.size() != 1) {
            Match.error("Set needs a key and value to set\nset(foo = \"bar\"");
        }
        mKey = parameters.keySet().iterator().next();
    }

    public String resolve() {
        String value = getParameter(mKey).resolve();
        mTarget.setProperty(mKey, value);
        return value;
    }
}
