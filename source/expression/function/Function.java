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
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public abstract class Function extends Expression implements IFunction {

    public static final String ANONYMOUS = "_";
    public static final String CLASS_OUTPUT = "out/java/classes";
    public static final String DIRECTORY = "directory";
    public static final String JAR_OUTPUT = "out/java/jar";
    public static final String LIBRARY = "library";
    public static final String MAIN_CLASS = "main_class";
    public static final String NAME = "name";
    public static final String PATTERN = "pattern";
    public static final String SOURCE = "source";
    public static final String VALUE = "value";

    private Map<String, IExpression> mParameters = new HashMap<String, IExpression>();

    public Function(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target);
        mParameters = parameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        mMatch.error("Function does not resolve to a single String");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasParameter(String key) {
        return mParameters.containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
            match.error(new Exception(String.format("couldn't load function \"%s\"", name), e));
            return null;
        }
    }

}
