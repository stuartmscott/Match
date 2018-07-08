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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import match.IMatch;
import match.ITarget;
import match.Utilities;

public class AndroidInstrumentation extends Function {

    public static final String ADB_INSTALL = "%sadb install -r %s";
    public static final String ADB_INSTRUMENT = "%sadb shell am instrument -w %s/%s 2>&1 | tee %s";
    public static final String APK = "apk";
    public static final String APK_TEST = "apk-test";
    public static final String PACKAGE = "package";
    public static final String PACKAGE_TEST = "package-test";
    public static final String PLATFORM_TOOLS = "%s/platform-tools/";
    public static final String RESULT_OUTPUT = "out/android/results/";
    public static final String RUNNER = "runner";
    public static final String SDK_LOCATION = "android-sdk-location";

    private String name;
    private IExpression apk;
    private IExpression apkTest;
    private String pkg;
    private String pkgTest;
    private String runner;
    private String output;
    private File outputFile;

    /**
     * Initializes the function with the given parameters.
     */
    public AndroidInstrumentation(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        IExpression n = getParameter(NAME);
        if (!(n instanceof Literal)) {
            match.error("AndroidInstrumentation function expects a String name");
        }
        name = n.resolve();
        target.setName(name);
        apk = getParameter(APK);
        apkTest = getParameter(APK_TEST);
        pkg = getParameter(PACKAGE).resolve();
        pkgTest = getParameter(PACKAGE_TEST).resolve();
        runner = getParameter(RUNNER).resolve();
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
        apk.configure();
        apkTest.configure();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        String sdkLocation = match.getProperty(SDK_LOCATION);
        String platformToolsDir = String.format(PLATFORM_TOOLS, sdkLocation);
        target.runCommand(String.format(MKDIR_COMMAND, RESULT_OUTPUT));

        for (IExpression e : new IExpression[] {apk, apkTest}) {
            for (String apk : e.resolveList()) {
                String file = match.getProperty(apk);
                match.awaitFile(file);
                if (target.runCommand(String.format(ADB_INSTALL, platformToolsDir, file)) != 0) {
                    match.error("Could not install " + file);
                }
            }
        }

        if (target.runCommand(String.format(ADB_INSTRUMENT, platformToolsDir, pkgTest, runner, output)) == 0) {
            match.provideFile(outputFile);
            // TODO uninstall instrumentation apk
        } else {
            // TODO consider throwing an error to stop Match
        }
        return output;
    }
}