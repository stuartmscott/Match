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
import main.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JavaJar extends Function {

    private static final String MKDIR_COMMAND = "mkdir -p %s";
    private static final String ECHO_COMMAND = "echo \"Manifest-Version: 1.0\nMain-Class: %s\n%s\" > %s";
    private static final String JAVAC_COMMAND = "javac %s %s -d %s";
    private static final String JAR_COMMAND = "jar cfm %s %s -C %s .";

    private IExpression mSource;
    private IExpression mMainClass;
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
        mSource = getParameter(SOURCE);
        mMainClass = getParameter(MAIN_CLASS);
        mOutput = String.format("%s/%s.jar", JAR_OUTPUT, mName);
        mIntermediate = String.format("%s/%s", CLASS_OUTPUT, mName);
        mManifest = String.format("%s/MANIFEST.MF", mIntermediate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        mMatch.addFile(mManifest);
        mMatch.addFile(mOutput);
        mMatch.setProperty(mName, mOutput);
        mSource.configure();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        String directories = String.format("{%s,%s}", mIntermediate, JAR_OUTPUT);
        List<String> libraries = new ArrayList<>();
        String javacClasspath = "";
        String jarClasspath = "";
        if (hasParameter(LIBRARY)) {
            for (String library : getParameter(LIBRARY).resolveList()) {
                libraries.add(mMatch.getProperty(library));
            }
            javacClasspath = String.format("-cp %s", Utilities.join(":", libraries));
            jarClasspath = String.format("Class-Path: %s\n", Utilities.join(":", libraries));
        }
        String files = Utilities.join(" ", mSource.resolveList());
        mMatch.runCommand(String.format(MKDIR_COMMAND, directories));
        mMatch.runCommand(String.format(ECHO_COMMAND, mMainClass.resolve(), jarClasspath, mManifest));
        mMatch.runCommand(String.format(JAVAC_COMMAND, javacClasspath, files, mIntermediate));
        mMatch.runCommand(String.format(JAR_COMMAND, mOutput, mManifest, mIntermediate));
        mMatch.provideFile(mOutput);
        return mOutput;
    }
}
