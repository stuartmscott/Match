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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import match.IMatch;
import match.ITarget;
import match.Utilities;
import match.expression.IExpression;
import match.expression.Literal;

/**
 * A function to run JUnit tests.
 */
public class JavaJUnit extends Function {

    public static final String MAIN_CLASS = "main-class";
    public static final String RESULT_OUTPUT = "out/java/results/";
    public static final String RUN_COMMAND = "java %s org.junit.runner.JUnitCore %s 2>&1 | tee %s";

    private String name;
    private String mainClass;
    private String output;
    private File outputFile;

    /**
     * Initializes the function with the given parameters.
     */
    public JavaJUnit(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        IExpression n = getParameter(NAME);
        if (!(n instanceof Literal)) {
            match.error("JavaJar function expects a String name");
        }
        name = n.resolve();
        target.setName(name);
        IExpression c = getParameter(MAIN_CLASS);
        if (!(c instanceof Literal)) {
            match.error("JavaJar function expects a String main-class");
        }
        mainClass = c.resolve();
        outputFile = new File(target.getDirectory(), RESULT_OUTPUT + name);
        output = outputFile.toPath().normalize().toAbsolutePath().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        match.addFile(output);
        match.setProperty(name, output);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        List<String> libraries = new ArrayList<>();
        Set<String> libs = new HashSet<>();
        libs.add("junit");
        libs.add("hamcrest-core");
        libs.add("mockito-all");
        if (hasParameter(LIBRARY)) {
            for (String library : getParameter(LIBRARY).resolveList()) {
                libs.add(library);
            }
        }
        for (String library : libs) {
            String path = match.getProperty(library);
            match.awaitFile(path);
            libraries.add(path);
        }
        String classpath = String.format("-cp %s", Utilities.join(":", libraries));
        target.runCommand(String.format(MKDIR_COMMAND, RESULT_OUTPUT));
        if (target.runCommand(String.format(RUN_COMMAND, classpath, mainClass, output)) == 0) {
            match.provideFile(outputFile);
        } else {
            match.error("JUnit failed");
        }
        return output;
    }
}
