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
import main.Match;

import java.util.Map;

public class GetFile extends Get {

    private String mKey;

    public GetFile(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        String parameterName = parameters.containsKey(NAME) ? NAME : ANONYMOUS;
        IExpression key = getParameter(parameterName);
        if (!(key instanceof Literal)) {
            mMatch.error("GetFile expects a String key");
        }
        mKey = key.resolve();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        String file = mMatch.getProperty(mKey);
        mMatch.awaitFile(file);
        return file;
    }
}
