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

    private static Map<String, Class<? extends Function>> sFunctions = new HashMap<String, Class<? extends Function>>();

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
            Match.error("missing parameter %s", key);
        }
        return parameter;
    }

    static void register(Class<? extends Function> clazz, String name) {
        sFunctions.put(name, clazz);
    }

    static Function getFunction(String name, IMatch match, ITarget target, Map<String, IExpression> parameters) throws Exception {
        Class<? extends Function> clazz = sFunctions.get(name);
        Constructor<? extends Function> constructor = clazz.getDeclaredConstructor(IMatch.class, ITarget.class, Map.class);
        return constructor.newInstance(match, target, parameters);
    }
}
