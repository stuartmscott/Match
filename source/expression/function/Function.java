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

import expression.Expression;
import expression.IExpression;
import main.IMatch;
import main.ITarget;
import main.Match;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public abstract class Function extends Expression implements IFunction {

    public static final String ANONYMOUS = "_";

    private Map<String, IExpression> mParameters = new HashMap<String, IExpression>();

    public Function(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target);
        mParameters = parameters;
    }

    /**
     * {inheritDoc}
     */
    public boolean hasParameter(String key) {
        return mParameters.containsKey(key);
    }

    /**
     * {inheritDoc}
     */
    public IExpression getParameter(String key) {
        IExpression parameter =  mParameters.get(key);
        if (parameter == null) {
            mMatch.error(String.format("missing parameter %s", key));
        }
        return parameter;
    }

    public static Function getFunction(String name, IMatch match, ITarget target, Map<String, IExpression> parameters) {
        try {
            String[] parts = name.split("_");
            StringBuilder clazzName = new StringBuilder("expression.function.");
            for (String part : parts) {
                // Capitalize first character
                clazzName.append(part.substring(0, 1).toUpperCase() + part.substring(1));
            }
            Class<?> clazz = Class.forName(clazzName.toString());
            Constructor<?> constructor = clazz.getDeclaredConstructor(IMatch.class, ITarget.class, Map.class);
            return (Function) constructor.newInstance(match, target, parameters);
        } catch (Exception e) {
            match.error(String.format("couldn't load function \"%s\"", name));
            return null;
        }
    }
}
