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
import java.net.URL;
import java.util.Map;

import match.IMatch;
import match.ITarget;
import match.Utilities;
import match.expression.IExpression;
import match.expression.Literal;

/**
 * A function to register an external library with the build system,
 * optionally downloading the library if it does not exist.
 */
public class Library extends Function {

    private static final String CURL_COMMAND = "curl %s -o %s";

    private final File directory;
    private final String name;
    private final IExpression filename;
    private IExpression url;
    private File file;
    private String path;

    // JUnit - http://search.maven.org/remotecontent?filepath=junit/junit/4.12/junit-4.12.jar
    // Hamcrest - http://search.maven.org/remotecontent?filepath=org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar
    // Mockito - http://search.maven.org/remotecontent?filepath=org/mockito/mockito-core/2.15.0/mockito-core-2.15.0.jar
    // Protobuf - http://search.maven.org/remotecontent?filepath=com/google/protobuf/protobuf-java/3.5.1/protobuf-java-3.5.1.jar

    /**
     * Initializes the function with the given parameters.
     */
    public Library(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        directory = match.getLibrariesDir();
        directory.mkdirs();
        IExpression n = getParameter(NAME);
        if (!(n instanceof Literal)) {
            match.error("Library function expects a String name");
        }
        name = n.resolve();
        target.setName("Library:" + name);
        filename = getParameter(FILE);
        if (hasParameter(LOCATION)) {
            url = getParameter(LOCATION);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        filename.configure();
        if (url != null) {
            url.configure();
        }
        file = new File(directory, filename.resolve());
        path = file.toPath().normalize().toAbsolutePath().toString();
        match.setProperty(name, path);
        match.addFile(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        if (!file.exists()) {
            if (url != null) {
                try {
                    target.runCommand(String.format(CURL_COMMAND, new URL(url.resolve() + filename).toString(), path));
                } catch (Exception e) {
                    match.error(e);
                }
            } else {
                match.error(path + " does not exist");
            }
        }
        if (file.exists()) {
            match.provideFile(file);
        }
        return path;
    }
}
