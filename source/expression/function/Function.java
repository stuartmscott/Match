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
import match.IMatch;
import match.ITarget;

import java.io.File;
import java.lang.reflect.Constructor;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public abstract class Function extends Expression implements IFunction {

    public final static String ANONYMOUS = "-";
    public final static String CLASS_OUTPUT = "out/java/classes/";
    public final static String DIRECTORY = "directory";
    public final static String EXTENSION = "extension";
    public final static String FILE = "file";
    public final static String JAR_OUTPUT = "out/java/jar/";
    public final static String LIBRARY = "library";
    public final static String LOCATION = "location";
    public final static String MANIFEST_OUTPUT = "out/java/manifest/";
    public final static String MKDIR_COMMAND = "mkdir -p %s";
    public final static String NAME = "name";
    public final static String PATTERN = "pattern";
    public final static String PROTO = "proto";
    public final static String PROTO_LITE = "proto-lite";
    public final static String PROTO_OUTPUT = "out/java/proto/";
    public final static String RESOURCE = "resource";
    public final static String SOURCE = "source";
    public final static String VALUE = "value";
    public final static String VERSION = "version";
    public final static String ZIP_OUTPUT = "out/zip/";

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
            mMatch.error(String.format("%s missing parameter %s", mTarget.getName(), key));
        }
        return parameter;
    }

    public static Function getFunction(String name, IMatch match, ITarget target, Map<String, IExpression> parameters) {
        try {
            String className = String.format("expression.function.%s", name);
            Class<?> clazz = Class.forName(className);
            Constructor<?> constructor = clazz.getDeclaredConstructor(IMatch.class, ITarget.class, Map.class);
            return (Function) constructor.newInstance(match, target, parameters);
        } catch (Exception e) {
            match.error(new Exception(String.format("couldn't load function \"%s\"", name), e));
            return null;
        }
    }
}
