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
    private static final String ARCHITECTURE = "architecture";// "x86_64"
    private static final String ANDROID = "android";
    private static final String LINUX = "linux";
    private static final String MAC = "mac";
    private static final String WINDOWS = "windows";

    private final File mDir;
    private final String mName;
    private final String mVersion;
    private final String mExtension;
    private final File mFile;
    private final String mFilePath;
    private URL mURL;

    public Library(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        mDir = match.getLibrariesDir();
        mDir.mkdirs();
        StringBuilder sb = new StringBuilder();
        IExpression name = getParameter(NAME);
        if (!(name instanceof Literal)) {
            mMatch.error("Library function expects a String name");
        }
        mName = name.resolve();
        target.setName("Library:" + mName);
        sb.append(mName);
        if (hasParameter(ANDROID) && Utilities.isAndroid()) {
            IExpression android = getParameter(ANDROID);
            if (!(android instanceof Literal)) {
                mMatch.error("Library function expects a String android");
            }
            sb.append("-");
            sb.append(android.resolve());
        }
        if (hasParameter(LINUX) && Utilities.isLinux()) {
            IExpression linux = getParameter(LINUX);
            if (!(linux instanceof Literal)) {
                mMatch.error("Library function expects a String linux");
            }
            sb.append("-");
            sb.append(linux.resolve());
        }
        if (hasParameter(MAC) && Utilities.isMac()) {
            IExpression mac = getParameter(MAC);
            if (!(mac instanceof Literal)) {
                mMatch.error("Library function expects a String mac");
            }
            sb.append("-");
            sb.append(mac.resolve());
        }
        if (hasParameter(WINDOWS) && Utilities.isWindows()) {
            IExpression windows = getParameter(WINDOWS);
            if (!(windows instanceof Literal)) {
                mMatch.error("Library function expects a String windows");
            }
            sb.append("-");
            sb.append(windows.resolve());
        }
        if (hasParameter(VERSION)) {
            IExpression version = getParameter(VERSION);
            if (!(version instanceof Literal)) {
                mMatch.error("Library function expects a String version");
            }
            mVersion = version.resolve();
            sb.append("-");
            sb.append(mVersion);
        } else {
            mVersion = "";
        }
        IExpression extension = getParameter(EXTENSION);
        if (!(extension instanceof Literal)) {
            mMatch.error("Library function expects a String extension");
        }
        mExtension = extension.resolve();
        sb.append(".");
        sb.append(mExtension);
        String filename = sb.toString();
        if (hasParameter(LOCATION)) {
            IExpression location = getParameter(LOCATION);
            if (!(location instanceof Literal)) {
                mMatch.error("Library function expects a String URL");
            }
            try {
                // JUnit - http://search.maven.org/remotecontent?filepath=junit/junit/4.12/junit-4.12.jar
                // Hamcrest - http://search.maven.org/remotecontent?filepath=org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar
                // Mockito - http://search.maven.org/remotecontent?filepath=org/mockito/mockito-core/2.15.0/mockito-core-2.15.0.jar
                // Protobuf - http://search.maven.org/remotecontent?filepath=com/google/protobuf/protobuf-java/3.5.1/protobuf-java-3.5.1.jar
                mURL = new URL(String.format("%s%s/%s", location.resolve(), mVersion, filename));
            } catch (Exception e) {
                mMatch.error(e);
            }
        }
        mFile = new File(mDir, filename);
        mFilePath = mFile.toPath().normalize().toAbsolutePath().toString();
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
        if (!mFile.exists() && mURL != null) {
            mTarget.runCommand(String.format(CURL_COMMAND, mURL.toString(), mFilePath));
        }
        if (mFile.exists()) {
            mMatch.provideFile(mFile);
        }
        return mFilePath;
    }
}
