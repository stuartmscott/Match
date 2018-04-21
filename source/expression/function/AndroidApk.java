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

public class AndroidApk extends Function {

    // android-sdk-location android-build-tools-version resource-intermediate-directory manifest resource-directory android-sdk-location android-sdk-version
    public static final String AAPT_COMMAND = "%saapt package -f -m -J %s -M %s -S %s -I %s";
    public static final String API_VERSION = "api-version";
    public static final String APK_OUTPUT = "out/android/apk/";
    public static final String BUILD_TOOLS = "%s/build-tools/%s/";
    public static final String BUILD_TOOLS_VERSION = "build-tools-version";
    public static final String DEX_OUTPUT = "out/android/dex/";
    public static final String DX_COMMAND = "%sdx --dex --output=";
    public static final String JAVAC_COMMAND = "javac -bootclasspath %s %s %s -d %s";
    public static final String MANIFEST = "manifest";
    public static final String RESOURCE_DIRECTORY = "resource-directory";
    public static final String RESOURCE_OUTPUT = "out/android/resource/";
    public static final String PLATFORM = "%s/platforms/android-%s/android.jar";
    public static final String PROTO = "proto";
    public static final String PROTO_LITE = "proto-lite";
    public static final String SDK_LOCATION = "android-sdk-location";

    private IExpression mSource;
    private IExpression mProtoSource;
    private IExpression mResource;
    private String mName;
    private String mApiVersion;
    private String mBuildToolsVersion;
    private String mManifest;
    private String mResourceDirectory;
    private String mIntermediateClasses;
    private String mIntermediateDex;
    private String mIntermediateProtos;
    private String mIntermediateResources;
    private String mOutput;

    public AndroidApk(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        IExpression name = getParameter(NAME);
        if (!(name instanceof Literal)) {
            mMatch.error("AndroidApk function expects a String name");
        }
        mName = name.resolve();
        target.setName(mName);
        mSource = getParameter(SOURCE);
        mApiVersion = getParameter(API_VERSION).resolve();
        mBuildToolsVersion = getParameter(BUILD_TOOLS_VERSION).resolve();
        mOutput = APK_OUTPUT + mName + ".apk";
        mIntermediateClasses = String.format("%s%s/", CLASS_OUTPUT, mName);
        mIntermediateDex = String.format("%s%s/", DEX_OUTPUT, mName);
        mIntermediateResources = String.format("%s%s/", RESOURCE_OUTPUT, mName);
        if (hasParameter(MANIFEST)) {
            IExpression manifest = getParameter(MANIFEST);
            if (!(manifest instanceof Literal)) {
                mMatch.error("AndroidApk function expects a String manifest");
            }
            mManifest = manifest.resolve();
        } else {
            mManifest = "AndroidManifest.xml";
        }
        if (hasParameter(PROTO)) {
            mProtoSource = getParameter(PROTO);
            mIntermediateProtos = String.format("%s%s/", PROTO_OUTPUT, mName);
        }
        if (hasParameter(RESOURCE)) {
            mResource = getParameter(RESOURCE);
        }
        if (hasParameter(RESOURCE_DIRECTORY)) {
            IExpression resourceDir = getParameter(RESOURCE_DIRECTORY);
            if (!(resourceDir instanceof Literal)) {
                mMatch.error("AndroidApk function expects a String resource directory");
            }
            mResourceDirectory = resourceDir.resolve();
        } else {
            mResourceDirectory = "resource";
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        mMatch.addFile(mOutput);
        mMatch.setProperty(mName, mOutput);
        mSource.configure();
        if (mProtoSource != null) {
            mProtoSource.configure();
        }
        if (mResource != null) {
            mResource.configure();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        List<String> libraries = new ArrayList<String>();
        String javacClasspath = "";
        if (hasParameter(LIBRARY)) {
            for (String library : getParameter(LIBRARY).resolveList()) {
                String path = mMatch.getProperty(library);
                mMatch.awaitFile(path);
                libraries.add(path);
            }
            javacClasspath = String.format("-cp %s", Utilities.join(":", libraries));
        }
        mMatch.runCommand(String.format(MKDIR_COMMAND, mIntermediateClasses));
        mMatch.runCommand(String.format(MKDIR_COMMAND, mIntermediateDex));
        mMatch.runCommand(String.format(MKDIR_COMMAND, mIntermediateResources));
        mMatch.runCommand(String.format(MKDIR_COMMAND, APK_OUTPUT));

        Set<String> sources = new HashSet<String>();
        sources.addAll(mSource.resolveList());
        // Compile protos
        if (mProtoSource != null) {
            /* TODO
            File dir = mTarget.getFile().getParentFile();
            mMatch.runCommand(String.format(MKDIR_COMMAND, mIntermediateProtos));
            if (hasParameter(PROTO_LITE) && getParameter(PROTO_LITE).resolve().equals("true")) {
                mMatch.runCommand(String.format(PROTOC_LITE_COMMAND, dir, mIntermediateProtos, Utilities.join(" ", mProtoSource.resolveList())));
            } else {
                mMatch.runCommand(String.format(PROTOC_COMMAND, dir, mIntermediateProtos, Utilities.join(" ", mProtoSource.resolveList())));
            }
            File directory = new File(mIntermediateProtos);
            // Add to the build
            mMatch.addDirectory(directory);
            // Get the relative paths of all java files generated
            String path = directory.getAbsolutePath() + "/";
            Set<String> generated = new HashSet<String>();
            Find.scanFiles(directory, path, generated, Pattern.compile(".*.java"));
            sources.addAll(generated);
            */
        }
        File match = mTarget.getFile();
        File matchDir = match.getParentFile();
        File manifestFile = new File(matchDir, mManifest);
        File resourceDir = new File(matchDir, mResourceDirectory);
        String sdkLocation = mMatch.getProperty(SDK_LOCATION);
        String buildToolsDir = String.format(BUILD_TOOLS, sdkLocation, mBuildToolsVersion);
        String platform = String.format(PLATFORM, sdkLocation, mApiVersion);
        // Generate R.java
        mMatch.runCommand(String.format(AAPT_COMMAND, buildToolsDir, mIntermediateResources, manifestFile, resourceDir, platform));
        File directory = new File(mIntermediateResources);
        // Add to the build
        mMatch.addDirectory(directory);
        // Get the relative paths of all java files generated
        String path = directory.getAbsolutePath() + "/";
        Set<String> generated = new HashSet<String>();
        Find.scanFiles(directory, path, generated, Pattern.compile(".*.java"));
        sources.addAll(generated);

        // Compile Java
        mMatch.runCommand(String.format(JAVAC_COMMAND, platform, javacClasspath, Utilities.join(" ", sources), mIntermediateClasses));

        // Create classes.dex
        mMatch.runCommand(String.format(DX_COMMAND, buildToolsDir, mIntermediateDex, mIntermediateClasses));

        mMatch.provideFile(mOutput);
        return mOutput;
    }
}