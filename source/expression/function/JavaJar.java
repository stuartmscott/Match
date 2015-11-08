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

import expression.IExpression;
import expression.Literal;
import main.IMatch;
import main.ITarget;
import main.Match;
import main.Utilities;

import java.util.Map;

public class JavaJar extends Function {

    private static final String MANIFEST_VERSION = "Manifest-Version: 1.0";
    private static final String MANIFEST_MAIN_CLASS = "Main-Class: %s";
    private static final String MANIFEST_CLASS_PATH = "Class-Path: %s";
    private static final String ECHO_COMMAND = "echo %s";
    private static final String COMMAND = "mkdir -p %s && javac %s %s -d %s && cd %s && jar cfm %s %s %s";

    private String mName;
    private String mManifest;
    private String mIntermediate;
    private String mOutput;

    public JavaJar(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        IExpression name = getParameter(NAME);
        if (!(name instanceof Literal)) {
            mMatch.error("JavaJar function expects a String name");
        }
        mName = name.resolve();
        mOutput = String.format("%s/%s.jar", JAR_OUTPUT, mName);
        mIntermediate = String.format("%s/%s", CLASS_OUTPUT, mName);
        mManifest = String.format("%s/MANIFEST.MF", mIntermediate, mName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() {
        mMatch.addFile(mManifest);
        mMatch.addFile(mOutput);
        mMatch.setProperty(mName, mOutput);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        String directories = String.format("{%s,%s}", mIntermediate, JAR_OUTPUT);
        String classpath = "";
        if (hasParameter(LIBRARY)) {
            classpath = String.format("-cp %s", Utilities.join(":", getParameter(LIBRARY).resolveList()));
        }
        String files = Utilities.join(" ", getParameter(SOURCE).resolveList());
        mMatch.runCommand(String.format(COMMAND, directories, classpath, files, mIntermediate, mIntermediate, mOutput, mManifest, mIntermediate));
        mMatch.provideFile(mOutput);
        return mOutput;
    }
}
