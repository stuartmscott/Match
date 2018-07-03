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
import match.IMatch;
import match.ITarget;
import match.Utilities;
import java.io.File;
import java.net.URL;
import java.util.Map;

public class Library extends Function {

    private static final String CURL_COMMAND = "curl %s -o %s";
    private static final String FILE_LINUX = "file-linux";
    private static final String FILE_MAC = "file-mac";
    private static final String FILE_WINDOWS = "file-windows";
    private static final String URL_LINUX = "location-linux";
    private static final String URL_MAC = "location-mac";
    private static final String URL_WINDOWS = "location-windows";

    private final File mDir;
    private final String mName;
    private final File mFile;
    private final String mFilePath;
    private URL mURL;

    public Library(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        mDir = match.getLibrariesDir();
        mDir.mkdirs();
        IExpression name = getParameter(NAME);
        if (!(name instanceof Literal)) {
            mMatch.error("Library function expects a String name");
        }
        mName = name.resolve();
        target.setName("Library:" + mName);

        String filename = null;
        String location = null;
        if (hasParameter(FILE)) {
            IExpression file = getParameter(FILE);
            if (!(file instanceof Literal)) {
                mMatch.error("Library function expects a String file");
            }
            filename = file.resolve();
        }
        if (hasParameter(LOCATION)) {
            IExpression url = getParameter(LOCATION);
            if (!(url instanceof Literal)) {
                mMatch.error("Library function expects a String URL");
            }
            location = url.resolve();
        }

        // Platform overrides
        if (Utilities.isLinux()) {
            if (hasParameter(FILE_LINUX)) {
                IExpression file = getParameter(FILE_LINUX);
                if (!(file instanceof Literal)) {
                    mMatch.error("Library function expects a String file-linux");
                }
                filename = file.resolve();
            }
            if (hasParameter(URL_LINUX)) {
                IExpression url = getParameter(URL_LINUX);
                if (!(url instanceof Literal)) {
                    mMatch.error("Library function expects a String location-linux");
                }
                location = url.resolve();
            }
        }
        if (Utilities.isMac()) {
            if (hasParameter(FILE_MAC)) {
                IExpression file = getParameter(FILE_MAC);
                if (!(file instanceof Literal)) {
                    mMatch.error("Library function expects a String file-mac");
                }
                filename = file.resolve();
            }
            if (hasParameter(URL_MAC)) {
                IExpression url = getParameter(URL_MAC);
                if (!(url instanceof Literal)) {
                    mMatch.error("Library function expects a String location-mac");
                }
                location = url.resolve();
            }
        }
        if (Utilities.isWindows()) {
            if (hasParameter(FILE_WINDOWS)) {
                IExpression file = getParameter(FILE_WINDOWS);
                if (!(file instanceof Literal)) {
                    mMatch.error("Library function expects a String file-windows");
                }
                filename = file.resolve();
            }
            if (hasParameter(URL_WINDOWS)) {
                IExpression url = getParameter(URL_WINDOWS);
                if (!(url instanceof Literal)) {
                    mMatch.error("Library function expects a String location-windows");
                }
                location = url.resolve();
            }
        }

        if (filename == null) {
            mMatch.error("Library function expects a String file");
        }
        mFile = new File(mDir, filename);
        mFilePath = mFile.toPath().normalize().toAbsolutePath().toString();
        if (location != null) {
            try {
                // JUnit - http://search.maven.org/remotecontent?filepath=junit/junit/4.12/junit-4.12.jar
                // Hamcrest - http://search.maven.org/remotecontent?filepath=org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar
                // Mockito - http://search.maven.org/remotecontent?filepath=org/mockito/mockito-core/2.15.0/mockito-core-2.15.0.jar
                // Protobuf - http://search.maven.org/remotecontent?filepath=com/google/protobuf/protobuf-java/3.5.1/protobuf-java-3.5.1.jar
                mURL = new URL(location + filename);
            } catch (Exception e) {
                mMatch.error(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        mMatch.setProperty(mName, mFilePath);
        mMatch.addFile(mFilePath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        if (!mFile.exists()) {
            if (mURL != null) {
                mTarget.runCommand(String.format(CURL_COMMAND, mURL.toString(), mFilePath));
            } else {
                mMatch.error(mFilePath + " does not exist");
            }
        }
        if (mFile.exists()) {
            mMatch.provideFile(mFile);
        }
        return mFilePath;
    }
}
