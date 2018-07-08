/*
 * Copyright 2018 Stuart Scott
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

import java.io.File;
import java.util.Map;

import match.IMatch;
import match.ITarget;
import match.Utilities;

public class CheckStyle extends Function {

    public static final String CONFIG = "config";
    public static final String RESULT_OUTPUT = "out/java/results/";
    public static final String RUN_COMMAND = "java -jar %s -c %s %s 2>&1 | tee %s";

    private String name;
    private String config;
    private IExpression source;
    private String output;
    private File outputFile;

    /**
     * Initializes the function with the given parameters.
     */
    public CheckStyle(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        IExpression n = getParameter(NAME);
        if (!(n instanceof Literal)) {
            match.error("CheckStyle function expects a String name");
        }
        name = n.resolve();
        target.setName(name);
        IExpression c = getParameter(CONFIG);
        if (!(c instanceof Literal)) {
            match.error("CheckStyle function expects a String config");
        }
        config = c.resolve();
        source = getParameter(SOURCE);
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
        source.configure();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        String path = match.getProperty("checkstyle");
        match.awaitFile(path);
        target.runCommand(String.format(MKDIR_COMMAND, RESULT_OUTPUT));
        if (target.runCommand(String.format(RUN_COMMAND, path, config, Utilities.join(" ", source.resolveList()), output)) == 0) {
            match.provideFile(outputFile);
        } else {
            match.error("CheckStyle failed");
        }
        return output;
    }
}