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
import java.io.File;
import java.net.URL;
import java.util.Map;

public class Library extends Function {

    private static final String CURL_COMMAND = "curl %s -o %s";

    private final File mCacheDir;
    private final String mKey;
    private final String mVersion;
    private final String mExtension;
    private final File mFile;
    private final String mFilePath;
    private URL mURL;

    public Library(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        File root = match.getRootDir();
        mCacheDir = new File(root, "libraries/");
        mCacheDir.mkdirs();
        IExpression key = getParameter(NAME);
        IExpression version = getParameter(VERSION);
        IExpression extension = getParameter(EXTENSION);
        IExpression location = getParameter(LOCATION);
        if (!(key instanceof Literal)) {
            mMatch.error("Library function expects a String key");
        }
        if (!(version instanceof Literal)) {
            mMatch.error("Library function expects a String version");
        }
        if (!(extension instanceof Literal)) {
            mMatch.error("Library function expects a String extension");
        }
        if (!(location instanceof Literal)) {
            mMatch.error("Library function expects a String URL");
        }
        mKey = key.resolve();
        mVersion = version.resolve();
        mExtension = extension.resolve();
        String filename = String.format("%s-%s.%s", mKey, mVersion, mExtension);
        try {
            // JUnit - http://search.maven.org/remotecontent?filepath=junit/junit/4.12/junit-4.12.jar
            // Hamcrest - http://search.maven.org/remotecontent?filepath=org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar
            // Mockito - http://search.maven.org/remotecontent?filepath=org/mockito/mockito-core/2.15.0/mockito-core-2.15.0.jar
            // Protobuf - http://search.maven.org/remotecontent?filepath=com/google/protobuf/protobuf-java/3.5.1/protobuf-java-3.5.1.jar
            mURL = new URL(String.format("%s%s/%s", location.resolve(), mVersion, filename));
        } catch (Exception e) {
            mMatch.error(e);
        }
        mFile = new File(mCacheDir, filename);
        mFilePath = root.toPath().relativize(mFile.toPath()).toString();
        target.setName("Library:" + mKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        mMatch.setProperty(mKey, mFilePath);
        mMatch.addFile(mFilePath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        if (!mFile.exists()) {
            mMatch.runCommand(String.format(CURL_COMMAND, mURL.toString(), mFilePath));
        }
        mMatch.provideFile(mFilePath);
        return mFilePath;
    }
}
