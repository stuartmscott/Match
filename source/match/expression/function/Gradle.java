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

package match.expression.function;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import match.IMatch;
import match.ITarget;
import match.Utilities;
import match.expression.IExpression;
import match.expression.Literal;

/**
 * A function to trigger Gradle to build the given tasks.
 */
public class Gradle extends Function {

    public static final String GRADLE_COMMAND = "./gradlew %s %s";
    public static final String OUTPUT = "output";
    public static final String TASK = "task";

    private IExpression require;
    private IExpression task;
    private String name;
    private String output;
    private File outputFile;

    /**
     * Initializes the function with the given parameters.
     */
    public Gradle(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        IExpression n = getParameter(NAME);
        if (!(n instanceof Literal)) {
            match.error("Gradle function expects a String name");
        }
        name = n.resolve();
        target.setName(name);
        task = getParameter(TASK);
        if (hasParameter(REQUIRE)) {
            require = getParameter(REQUIRE);
        }
        IExpression o = getParameter(OUTPUT);
        if (!(o instanceof Literal)) {
            match.error("Gradle function expects a String output");
        }
        outputFile = new File(target.getDirectory(), o.resolve());
        output = outputFile.toPath().normalize().toAbsolutePath().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        if (!match.isCleaning()) {
            match.addFile(output);
            match.setProperty(name, output);
            task.configure();
            if (require != null) {
                require.configure();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        // Execute Gradle with task list
        List<String> tasks = new ArrayList<>();
        if (match.isCleaning()) {
            tasks.add("clean");
        } else {
            tasks.addAll(task.resolveList());

            // Await requirements
            Set<String> requirements = new HashSet<>();
            if (require != null) {
                requirements.addAll(require.resolveList());
            }
            for (String r : requirements) {
                String path = match.getProperty(r);
                match.awaitFile(path);
            }
        }
        StringBuilder options = new StringBuilder();
        if (match.isQuiet()) {
            options.append("-q");
        } else if (match.isVerbose()) {
            options.append("-i");
        }
        if (target.runCommand(String.format(GRADLE_COMMAND, options.toString(), Utilities.join(" ", tasks))) == 0) {
            if (!match.isCleaning()) {
                match.provideFile(outputFile);
            }
        } else {
            match.error("Gradle failed");
        }
        return output;
    }

}