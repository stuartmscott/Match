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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class Release extends Function {

    public final static String CHANNEL = "channel";
    public final static String CP_COMMAND = "cp %s %s";
    // TODO for f in out/java/jar/*.jar; do cp "$f" "/tmp/libraries/$(basename $f .jar)-0.1.jar"; done;

    private IExpression mChannel;
    private String mSource;
    private String mName;
    private String mOutputDir;
    private String mOutput;
    private File mOutputFile;

    public Release(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        IExpression source = getParameter(SOURCE);
        if (!(source instanceof Literal)) {
            mMatch.error("Release function expects a String source");
        }
        mSource = source.resolve();
        mChannel = getParameter(CHANNEL);
        String extension = "zip";
        if (hasParameter(EXTENSION)) {
            IExpression ext = getParameter(EXTENSION);
            if (!(ext instanceof Literal)) {
                mMatch.error("Release function expects a String extension");
            }
            extension = ext.resolve();
        }
        // TODO switch to YYYYMMDD
        float version = 0.1f;
        if (hasParameter(VERSION)) {
            IExpression ver = getParameter(VERSION);
            if (!(ver instanceof Literal)) {
                mMatch.error("Release function expects a float version");
            }
            version = Float.parseFloat(ver.resolve());
        }
        mName = String.format("%s-%1.1f.%s", mSource, version, extension);
        target.setName(mName);
        mOutputDir = String.format("%sv%1.1f/", RELEASE_OUTPUT, version);
        File directory = new File(target.getDirectory(), mOutputDir);
        mOutputFile = new File(directory, mName);
        mOutput = mOutputFile.toPath().normalize().toAbsolutePath().toString();
        // TODO ensure release isn't re-created if the inputs haven't been modified
        // TODO wait for test result before releasing - avoid releasing bad code
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        mMatch.addFile(mOutput);
        mMatch.setProperty(mName, mOutput);
        mChannel.configure();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        // Create output directory
        mTarget.runCommand(String.format(MKDIR_COMMAND, mOutputDir));
        // Get the source file
        String source = mMatch.getProperty(mSource);
        mMatch.awaitFile(source);
        // Create release
        mTarget.runCommand(String.format(CP_COMMAND, source, mOutput));
        // Push out distribution channels
        for (String channel : mChannel.resolveList()) {
            mTarget.runCommand(String.format(channel, mOutput));
        }
        mMatch.provideFile(mOutputFile);
        return mOutput;
    }
}
