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

import java.io.File;
import java.lang.reflect.Constructor;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import match.IMatch;
import match.ITarget;
import match.expression.Expression;
import match.expression.IExpression;

/**
 * Base class of build functions.
 */
public abstract class Function extends Expression implements IFunction {

    public static final String ANONYMOUS = "-";
    public static final String CLASS_OUTPUT = "out/java/classes/";
    public static final String DIRECTORY = "directory";
    public static final String EXTENSION = "extension";
    public static final String FILE = "file";
    public static final String JAR_OUTPUT = "out/java/jar/";
    public static final String JAVA_OUTPUT = "out/java/generated/";
    public static final String FIND_RM_COMMAND = "find %s -name %s -print0 | xargs -0 rm";
    public static final String LIBRARY = "library";
    public static final String LOCATION = "location";
    public static final String MANIFEST_OUTPUT = "out/java/manifest/";
    public static final String MKDIR_COMMAND = "mkdir -p %s";
    public static final String NAME = "name";
    public static final String PATTERN = "pattern";
    public static final String PROTO = "proto";
    public static final String PROTO_LITE = "proto-lite";
    public static final String REQUIRE = "require";
    public static final String RESOURCE = "resource";
    public static final String SOURCE = "source";
    public static final String VALUE = "value";
    public static final String VERSION = "version";
    public static final String ZIP_OUTPUT = "out/zip/";

    private Map<String, IExpression> parameters = new HashMap<String, IExpression>();

    /**
     * Initializes the function with the given parameters.
     */
    public Function(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target);
        this.parameters = parameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        match.error("Function does not resolve to a single String");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasParameter(String key) {
        return parameters.containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IExpression getParameter(String key) {
        IExpression parameter =  parameters.get(key);
        if (parameter == null) {
            match.error(String.format("%s missing parameter %s", target.getName(), key));
        }
        return parameter;
    }

    /**
     * Returns an instance of the function with the given name.
     */
    public static Function getFunction(String name, IMatch match, ITarget target, Map<String, IExpression> parameters) {
        try {
            String className = String.format("match.expression.function.%s", name);
            Class<?> clazz = Class.forName(className);
            Constructor<?> constructor = clazz.getDeclaredConstructor(IMatch.class, ITarget.class, Map.class);
            return (Function) constructor.newInstance(match, target, parameters);
        } catch (Exception e) {
            match.error(new Exception(String.format("couldn't load function \"%s\"", name), e));
            return null;
        }
    }
}
