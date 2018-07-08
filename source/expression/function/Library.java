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
import java.net.URL;
import java.util.Map;

import match.IMatch;
import match.ITarget;
import match.Utilities;

public class Library extends Function {

    private static final String CURL_COMMAND = "curl %s -o %s";
    private static final String FILE_LINUX = "file-linux";
    private static final String FILE_MAC = "file-mac";
    private static final String FILE_WINDOWS = "file-windows";
    private static final String URL_LINUX = "location-linux";
    private static final String URL_MAC = "location-mac";
    private static final String URL_WINDOWS = "location-windows";

    private final File directory;
    private final String name;
    private final File file;
    private final String filePath;
    private URL url;

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

        String filename = null;
        String location = null;
        if (hasParameter(FILE)) {
            IExpression file = getParameter(FILE);
            if (!(file instanceof Literal)) {
                match.error("Library function expects a String file");
            }
            filename = file.resolve();
        }
        if (hasParameter(LOCATION)) {
            IExpression u = getParameter(LOCATION);
            if (!(u instanceof Literal)) {
                match.error("Library function expects a String URL");
            }
            location = u.resolve();
        }

        // Platform overrides
        if (Utilities.isLinux()) {
            if (hasParameter(FILE_LINUX)) {
                IExpression file = getParameter(FILE_LINUX);
                if (!(file instanceof Literal)) {
                    match.error("Library function expects a String file-linux");
                }
                filename = file.resolve();
            }
            if (hasParameter(URL_LINUX)) {
                IExpression u = getParameter(URL_LINUX);
                if (!(u instanceof Literal)) {
                    match.error("Library function expects a String location-linux");
                }
                location = u.resolve();
            }
        }
        if (Utilities.isMac()) {
            if (hasParameter(FILE_MAC)) {
                IExpression file = getParameter(FILE_MAC);
                if (!(file instanceof Literal)) {
                    match.error("Library function expects a String file-mac");
                }
                filename = file.resolve();
            }
            if (hasParameter(URL_MAC)) {
                IExpression u = getParameter(URL_MAC);
                if (!(u instanceof Literal)) {
                    match.error("Library function expects a String location-mac");
                }
                location = u.resolve();
            }
        }
        if (Utilities.isWindows()) {
            if (hasParameter(FILE_WINDOWS)) {
                IExpression file = getParameter(FILE_WINDOWS);
                if (!(file instanceof Literal)) {
                    match.error("Library function expects a String file-windows");
                }
                filename = file.resolve();
            }
            if (hasParameter(URL_WINDOWS)) {
                IExpression u = getParameter(URL_WINDOWS);
                if (!(u instanceof Literal)) {
                    match.error("Library function expects a String location-windows");
                }
                location = u.resolve();
            }
        }

        if (filename == null) {
            match.error("Library function expects a String file");
        }
        file = new File(directory, filename);
        filePath = file.toPath().normalize().toAbsolutePath().toString();
        if (location != null) {
            try {
                // JUnit - http://search.maven.org/remotecontent?filepath=junit/junit/4.12/junit-4.12.jar
                // Hamcrest - http://search.maven.org/remotecontent?filepath=org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar
                // Mockito - http://search.maven.org/remotecontent?filepath=org/mockito/mockito-core/2.15.0/mockito-core-2.15.0.jar
                // Protobuf - http://search.maven.org/remotecontent?filepath=com/google/protobuf/protobuf-java/3.5.1/protobuf-java-3.5.1.jar
                url = new URL(location + filename);
            } catch (Exception e) {
                match.error(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        match.setProperty(name, filePath);
        match.addFile(filePath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        if (!file.exists()) {
            if (url != null) {
                target.runCommand(String.format(CURL_COMMAND, url.toString(), filePath));
            } else {
                match.error(filePath + " does not exist");
            }
        }
        if (file.exists()) {
            match.provideFile(file);
        }
        return filePath;
    }
}
