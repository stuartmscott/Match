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
    public static final String NAME = "name";

    private static Map<String, Class<? extends Function>> sFunctions = new HashMap<String, Class<? extends Function>>();

    public Function(IMatch match, ITarget target) {
        super(match, target);
    }

    /**
     * {@inheritDoc}
     */
    public void setUp() {}

    /**
     * {@inheritDoc}
     */
    public void tearDown() {}

    static void register(Class<? extends Function> clazz, String name) {
        sFunctions.put(name, clazz);
    }

    static Function getFunction(String name, IMatch match, ITarget target, Map<String, IExpression> parameters) throws Exception {
        Class<? extends Function> clazz = sFunctions.get(name);
        Constructor<? extends Function> constructor = clazz.getDeclaredConstructor(IMatch.class, ITarget.class, Map.class);
        return constructor.newInstance(match, target, parameters);
    }

    /**
     * Gets the parameter for the given key.
     */
    static IExpression getParameter(Map<String, IExpression> parameters, String key) {
        IExpression parameter =  parameters.get(key);
        if (parameter == null) {
            Match.error("missing parameter %s", key);
        }
        return parameter;
    }

}
