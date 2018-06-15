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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

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

    private String mName;
    private String mApk;
    private String mApkTest;
    private String mPackage;
    private String mPackageTest;
    private String mRunner;
    private String mOutput;
    private File mOutputFile;

    public AndroidInstrumentation(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        IExpression name = getParameter(NAME);
        if (!(name instanceof Literal)) {
            mMatch.error("AndroidInstrumentation function expects a String name");
        }
        mName = name.resolve();
        target.setName(mName);
        mApk = getParameter(APK).resolve();
        mApkTest = getParameter(APK_TEST).resolve();
        mPackage = getParameter(PACKAGE).resolve();
        mPackageTest = getParameter(PACKAGE_TEST).resolve();
        mRunner = getParameter(RUNNER).resolve();
        mOutputFile = new File(target.getDirectory(), RESULT_OUTPUT + mName);
        mOutput = mOutputFile.toPath().normalize().toAbsolutePath().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        mMatch.addFile(mOutput);
        mMatch.setProperty(mName, mOutput);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        String sdkLocation = mMatch.getProperty(SDK_LOCATION);
        String platformToolsDir = String.format(PLATFORM_TOOLS, sdkLocation);
        mTarget.runCommand(String.format(MKDIR_COMMAND, RESULT_OUTPUT));

        String apk = mMatch.getProperty(mApk);
        String apkTest = mMatch.getProperty(mApkTest);
        mMatch.awaitFile(apk);
        mMatch.awaitFile(apkTest);

        if (mTarget.runCommand(String.format(ADB_INSTALL, platformToolsDir, apk)) == 0 &&
                mTarget.runCommand(String.format(ADB_INSTALL, platformToolsDir, apkTest)) == 0 &&
                mTarget.runCommand(String.format(ADB_INSTRUMENT, platformToolsDir, mPackageTest, mRunner, mOutput)) == 0) {
            mMatch.provideFile(mOutputFile);
        } else {
            // TODO consider throwing an error to stop Match
        }
        return mOutput;
    }
}