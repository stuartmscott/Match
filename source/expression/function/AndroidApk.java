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

    public static final String AAPT_ADD_COMMAND = "%saapt add -f %s %s";
    public static final String AAPT_ADD_K_COMMAND = "%saapt add -k -f %s %s";
    public static final String AAPT_APK_COMMAND = "%saapt package -f -m -F %s -M %s -I %s";
    public static final String AAPT_APK_RES_COMMAND = "%saapt package -f -m -F %s -M %s -S %s -I %s";
    public static final String AAPT_RES_COMMAND = "%saapt package -f -m -J %s -M %s -S %s -I %s";
    public static final String API_VERSION = "api-version";
    public static final String APK_OUTPUT = "out/android/apk/";
    public static final String APK_SIGNER_COMMAND = "%sapksigner sign --ks %s --ks-pass file:%s --out %s %s";
    public static final String BUILD_TOOLS = "%s/build-tools/%s/";
    public static final String BUILD_TOOLS_VERSION = "build-tools-version";
    public static final String DEX_OUTPUT = "out/android/dex/";
    public static final String DX_COMMAND = "%sdx --dex --output=%s %s %s";
    public static final String JAR_COMMAND = "jar cf %s -C %s .";
    public static final String JAVAC_COMMAND = "javac -bootclasspath %s %s -sourcepath %s %s -d %s";
    public static final String KEYSTORE = "keystore";
    public static final String KEYSTORE_PASSWORD_FILE = "keystore-password-file";
    public static final String MANIFEST = "manifest";
    public static final String RESOURCE_DIRECTORY = "resource-directory";
    public static final String RESOURCE_OUTPUT = "out/android/res/";
    public static final String PLATFORM = "%s/platforms/android-%s/android.jar";
    public static final String PROTO = "proto";
    public static final String PROTO_LITE = "proto-lite";
    public static final String SDK_LOCATION = "android-sdk-location";
    public static final String ZIP_ALIGN_COMMAND = "%szipalign -f 4 %s %s";

    private IExpression mSource;
    private IExpression mProtoSource;
    private IExpression mResource;
    private String mName;
    private String mApiVersion;
    private String mBuildToolsVersion;
    private String mKeyStore;
    private String mKeyStorePassword;
    private String mManifest;
    private String mResourceDirectory;
    private String mIntermediateApks;
    private String mIntermediateClasses;
    private String mIntermediateJars;
    private String mIntermediateDex;
    private String mIntermediateProtos;
    private String mIntermediateResources;
    private String mOutputApk;
    private File mOutputApkFile;
    private String mOutputJar;
    private File mOutputJarFile;

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
        mKeyStore = getParameter(KEYSTORE).resolve();
        mKeyStorePassword = getParameter(KEYSTORE_PASSWORD_FILE).resolve();
        mIntermediateApks = String.format("%s%s/", APK_OUTPUT, mName);
        mIntermediateClasses = String.format("%s%s/", CLASS_OUTPUT, mName);
        mIntermediateJars = String.format("%s%s/", JAR_OUTPUT, mName);
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
        }
        mOutputApkFile = new File(target.getDirectory(), mIntermediateApks + mName + ".apk");
        mOutputApk = mOutputApkFile.toPath().normalize().toAbsolutePath().toString();
        mOutputJarFile = new File(target.getDirectory(), mIntermediateJars + mName + ".jar");
        mOutputJar = mOutputJarFile.toPath().normalize().toAbsolutePath().toString();
        // TODO create an apk intermediate directory in which classes.dex, resources,
        // libraries and other files are added with the correct directory path.
        // Then when the APK is created, everything should be in the right place
        // TODO if Android build is still a pain, consider just invoking gradle although
        // this will not help Match determine file dependancies.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        mMatch.addFile(mOutputApk);
        mMatch.setProperty(mName, mOutputApk);
        mMatch.addFile(mOutputJar);
        mMatch.setProperty(mName + "-jar", mOutputJar);
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
        File matchDir = mTarget.getFile().getParentFile();
        String sdkLocation = mMatch.getProperty(SDK_LOCATION);
        String buildToolsDir = String.format(BUILD_TOOLS, sdkLocation, mBuildToolsVersion);
        String platform = String.format(PLATFORM, sdkLocation, mApiVersion);
        String classesDex = String.format("%sclasses.dex", mIntermediateDex);
        String alignedApk = String.format("%s%s.aligned.apk", mIntermediateApks, mName);
        String unalignedApk = String.format("%s%s.unaligned.apk", mIntermediateApks, mName);
        List<String> libraries = new ArrayList<>();
        String javacClasspath = "";
        if (hasParameter(LIBRARY)) {
            for (String library : getParameter(LIBRARY).resolveList()) {
                String path = mMatch.getProperty(library);
                mMatch.awaitFile(path);
                libraries.add(path);
            }
            javacClasspath = String.format("-cp %s", Utilities.join(":", libraries));
        }
        mTarget.runCommand(String.format(MKDIR_COMMAND, mIntermediateApks));
        mTarget.runCommand(String.format(MKDIR_COMMAND, mIntermediateClasses));
        mTarget.runCommand(String.format(MKDIR_COMMAND, mIntermediateJars));
        mTarget.runCommand(String.format(MKDIR_COMMAND, mIntermediateDex));
        mTarget.runCommand(String.format(MKDIR_COMMAND, mIntermediateResources));

        Set<String> sources = new HashSet<>();
        sources.addAll(mSource.resolveList());
        // Compile protos
        if (mProtoSource != null) {
            /* TODO
            File dir = mTarget.getFile().getParentFile();
            mTarget.runCommand(String.format(MKDIR_COMMAND, mIntermediateProtos));
            if (hasParameter(PROTO_LITE) && getParameter(PROTO_LITE).resolve().equals("true")) {
                mTarget.runCommand(String.format(PROTOC_LITE_COMMAND, dir, mIntermediateProtos, Utilities.join(" ", mProtoSource.resolveList())));
            } else {
                mTarget.runCommand(String.format(PROTOC_COMMAND, dir, mIntermediateProtos, Utilities.join(" ", mProtoSource.resolveList())));
            }
            File directory = new File(mIntermediateProtos);
            // Add to the build
            mMatch.addDirectory(directory);
            // Get the relative paths of all java files generated
            String path = directory.toPath().toString() + "/";
            Set<String> generated = new HashSet<>();
            Find.scanFiles(directory, path, generated, Pattern.compile(".*.java"));
            sources.addAll(generated);
            */
        }

        // Generate R.java
        if (mResourceDirectory != null) {
            mTarget.runCommand(String.format(AAPT_RES_COMMAND, buildToolsDir, mIntermediateResources, mManifest, mResourceDirectory, platform));
        }

        // Add to the build
        mMatch.addDirectory(new File(matchDir, mIntermediateResources));

        // Compile Java
        mTarget.runCommand(String.format(JAVAC_COMMAND, platform, javacClasspath, mIntermediateResources, Utilities.join(" ", sources), mIntermediateClasses));

        // Add to the build
        mMatch.addDirectory(new File(matchDir, mIntermediateClasses));

        // Package jar
        mTarget.runCommand(String.format(JAR_COMMAND, mOutputJar, mIntermediateClasses));

        System.out.println("Android Jar " + mOutputJar);
        mMatch.provideFile(mOutputJarFile);

        // Create classes.dex
        mTarget.runCommand(String.format(DX_COMMAND, buildToolsDir, classesDex, Utilities.join(" ", libraries), mIntermediateClasses));

        // Generate APK
        if (mResourceDirectory != null) {
            mTarget.runCommand(String.format(AAPT_APK_RES_COMMAND, buildToolsDir, unalignedApk, mManifest, mResourceDirectory, platform));
        } else {
            mTarget.runCommand(String.format(AAPT_APK_COMMAND, buildToolsDir, unalignedApk, mManifest, platform));
        }

        // Add classes.dex
        mTarget.runCommand(String.format(AAPT_ADD_K_COMMAND, buildToolsDir, unalignedApk, classesDex));

        // Add resources
        if (mResource != null) {
            for (String resource : mResource.resolveList()) {
                mTarget.runCommand(String.format(AAPT_ADD_COMMAND, buildToolsDir, unalignedApk, resource));
            }
        }
        // Align APK
        mTarget.runCommand(String.format(ZIP_ALIGN_COMMAND, buildToolsDir, unalignedApk, alignedApk));

        // Sign APK
        mTarget.runCommand(String.format(APK_SIGNER_COMMAND, buildToolsDir, mKeyStore, mKeyStorePassword, mOutputApk, alignedApk));
        System.out.println("Android APK " + mOutputApk);
        mMatch.provideFile(mOutputApkFile);
        return mOutputApk;
    }
}