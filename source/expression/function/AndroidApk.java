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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class AndroidApk extends Function {

    public static final String AAPT_PACKAGE_COMMAND = "%saapt package -v -m -f -F %s -M %s -J %s --output-text-symbols %s -P %s/public_resources.xml -G %s/proguard.txt -I %s %s";
    public static final String AAPT_PACKAGE_LIB_COMMAND = "%saapt package -v -m -f -F %s -M %s -J %s --non-constant-id --output-text-symbols %s -P %s/public_resources.xml -G %s/proguard.txt -I %s %s";
    public static final String AAPT2_COMPILE_COMMAND = "%saapt2 compile -o %s %s";
    public static final String AAPT2_LINK_COMMAND = "%saapt2 link --auto-add-overlay -o %s --manifest %s --java %s -I %s %s %s";
    public static final String AAPT2_LINK_LIB_COMMAND = "%saapt2 link --auto-add-overlay --static-lib -o %s --manifest %s --java %s -I %s %s %s";
    public static final String AAR_OUTPUT = "out/android/aar/";
    public static final String API_VERSION = "api-version";
    public static final String APK_OUTPUT = "out/android/apk/";
    public static final String APK_SIGNER_COMMAND = "%sapksigner sign --ks %s --ks-pass file:%s --out %s %s";
    public static final String ASSET_DIRECTORY = "asset-directory";
    public static final String BUILD_TOOLS = "%s/build-tools/%s/";
    public static final String BUILD_TOOLS_VERSION = "build-tools-version";
    public static final String CLASSES_JAR = "classes.jar";
    public static final String COPY_COMMAND = "cp -R %s %s";
    public static final String DEX_OUTPUT = "out/android/dex/";
    public static final String DX_COMMAND = "%sdx --dex --output=%s %s %s";
    public static final String JAR_COMMAND = "jar cf %s -C %s %s";
    public static final String JAVAC_COMMAND = "javac -bootclasspath %s %s -sourcepath %s %s -d %s";
    public static final String JAVA_OUTPUT = "out/android/java/";
    public static final String IS_LIBRARY = "is-library";
    public static final String KEYSTORE = "keystore";
    public static final String KEYSTORE_PASSWORD_FILE = "keystore-password-file";
    public static final String MANIFEST = "manifest";
    public static final String RESOURCE_DIRECTORY = "resource-directory";
    public static final String RESOURCE_OUTPUT = "out/android/res/";
    public static final String PLATFORM = "%s/platforms/android-%s/android.jar";
    public static final String SDK_LOCATION = "android-sdk-location";
    public static final String UNZIP_ALL_COMMAND = "unzip -nq %s -d %s";
    public static final String UNZIP_FILE_COMMAND = "unzip -jq %s %s -d %s";
    public static final String USE_AAPT2 = "use-aapt2";
    public static final String ZIP_ADD_COMMAND = "zip -jX %s %s";
    public static final String ZIP_ALIGN_COMMAND = "%szipalign -f 4 %s %s";

    private IExpression mSource;
    private IExpression mProtoSource;
    private IExpression mAssetDirs;
    private IExpression mResourceDirs;
    private boolean mIsLibrary = false;
    private boolean mUseAapt2 = false;
    private String mName;
    private String mApiVersion;
    private String mBuildToolsVersion;
    private String mKeyStore;
    private String mKeyStorePassword;
    private String mManifest;
    private String mIntermediateAars;
    private String mIntermediateApks;
    private String mIntermediateClasses;
    private String mIntermediateDex;
    private String mIntermediateJars;
    private String mIntermediateJava;
    private String mIntermediateProtos;
    private String mIntermediateResources;
    private String mOutput;
    private File mOutputFile;
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
        mIntermediateAars = String.format("%s%s/", AAR_OUTPUT, mName);
        mIntermediateApks = String.format("%s%s/", APK_OUTPUT, mName);
        mIntermediateClasses = String.format("%s%s/", CLASS_OUTPUT, mName);
        mIntermediateDex = String.format("%s%s/", DEX_OUTPUT, mName);
        mIntermediateJars = String.format("%s%s/", JAR_OUTPUT, mName);
        mIntermediateJava = String.format("%s%s/", JAVA_OUTPUT, mName);
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
        if (hasParameter(ASSET_DIRECTORY)) {
            mAssetDirs = getParameter(ASSET_DIRECTORY);
        }
        if (hasParameter(RESOURCE_DIRECTORY)) {
            mResourceDirs = getParameter(RESOURCE_DIRECTORY);
        }
        if (hasParameter(IS_LIBRARY)) {
            mIsLibrary = Boolean.parseBoolean(getParameter(IS_LIBRARY).resolve());
        }
        if (hasParameter(USE_AAPT2)) {
            // TODO use to determine what gets built and how
            mUseAapt2 = Boolean.parseBoolean(getParameter(USE_AAPT2).resolve());
        }
        if (mIsLibrary && !mUseAapt2) {
            mOutputFile = new File(target.getDirectory(), mIntermediateAars + mName + ".aar");
            mOutput = mOutputFile.toPath().normalize().toAbsolutePath().toString();
        } else {
            mOutputFile = new File(target.getDirectory(), mIntermediateApks + mName + ".apk");
            mOutput = mOutputFile.toPath().normalize().toAbsolutePath().toString();
        }
        mOutputJarFile = new File(target.getDirectory(), mIntermediateJars + mName + ".jar");
        mOutputJar = mOutputJarFile.toPath().normalize().toAbsolutePath().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        mMatch.addFile(mOutput);
        mMatch.setProperty(mName, mOutput);
        mMatch.addFile(mOutputJar);
        mMatch.setProperty(mName + "-jar", mOutputJar);
        mSource.configure();
        if (mProtoSource != null) {
            mProtoSource.configure();
        }
        if (mAssetDirs != null) {
            mAssetDirs.configure();
        }
        if (mResourceDirs != null) {
            mResourceDirs.configure();
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
        String unalignedApk = String.format("%s%s.unaligned.apk", mIntermediateApks, mName);
        Set<String> assetDirs = new HashSet<>();
        if (mAssetDirs != null) {
            assetDirs.addAll(mAssetDirs.resolveList());
        }
        Set<String> libraries = new HashSet<>();
        List<String> resources = new ArrayList<>();// Resources need to be ordered overlay first
        Set<String> resourceDirs = new HashSet<>();
        if (mResourceDirs != null) {
            resourceDirs.addAll(mResourceDirs.resolveList());
        }
        Set<String> sources = new HashSet<>();
        sources.addAll(mSource.resolveList());
        String javacClasspath = "";
        if (hasParameter(LIBRARY)) {
            for (String library : getParameter(LIBRARY).resolveList()) {
                String path = mMatch.getProperty(library);
                mMatch.awaitFile(path);
                if (path.endsWith(".aar")) {
                    // Extract classes.jar to temp file
                    try {
                        Path p = Files.createTempDirectory(library);
                        File dir = p.toFile();
                        dir.deleteOnExit();
                        mTarget.runCommand(String.format(UNZIP_FILE_COMMAND, path, CLASSES_JAR, p.normalize().toAbsolutePath().toString()));
                        File classesJar = new File(dir, CLASSES_JAR);
                        if (classesJar.exists()) {
                            libraries.add(classesJar.toPath().normalize().toAbsolutePath().toString());
                        }
                    } catch (Exception e) {
                        mMatch.error(e);
                    }
                } else if (path.endsWith(".apk")) {
                    // Add to the resources
                    resources.add(path);
                    // Get the libraries classes.jar
                    String jar = mMatch.getProperty(library + "-jar");
                    System.out.println("Apk: " + path + " Jar: " + jar);
                    mMatch.awaitFile(jar);
                    libraries.add(jar);
                } else if (path.endsWith(".jar")) {
                    libraries.add(path);
                } else {
                    mMatch.error("AndroidApk encountered unknown library type: " + path);
                }
            }
            javacClasspath = String.format("-cp %s", Utilities.join(":", libraries));
        }
        mTarget.runCommand(String.format(MKDIR_COMMAND, mIntermediateApks));
        mTarget.runCommand(String.format(MKDIR_COMMAND, mIntermediateClasses));
        mTarget.runCommand(String.format(MKDIR_COMMAND, mIntermediateDex));
        mTarget.runCommand(String.format(MKDIR_COMMAND, mIntermediateJars));
        mTarget.runCommand(String.format(MKDIR_COMMAND, mIntermediateJava));
        mTarget.runCommand(String.format(MKDIR_COMMAND, mIntermediateResources));

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

        if (mUseAapt2) {
            // Find all resource files
            Set<String> resourceFiles = new HashSet<>();
            for (String dir : resourceDirs) {
                Find.scanFiles(new File(matchDir, dir), dir + "/", resourceFiles, ".*");
            }
            // Compile resources
            for (String f : resourceFiles) {
                mTarget.runCommand(String.format(AAPT2_COMPILE_COMMAND, buildToolsDir, mIntermediateResources, f));
            }

            // Add to the build
            mMatch.addDirectory(new File(matchDir, mIntermediateResources));

            // Link APK
            StringBuilder resBuilder = new StringBuilder();
            Find.scanFiles(new File(matchDir, mIntermediateResources), mIntermediateResources, resources, ".*");
            for (String r : resources) {
                resBuilder.append(" -R ");
                resBuilder.append(r);
            }
            StringBuilder assetBuilder = new StringBuilder();
            for (String a : assetDirs) {
                assetBuilder.append(" -A ");
                assetBuilder.append(a);
            }
            mTarget.runCommand(String.format(mIsLibrary ? AAPT2_LINK_LIB_COMMAND : AAPT2_LINK_COMMAND, buildToolsDir, unalignedApk, mManifest, mIntermediateJava, platform, resBuilder.toString(), assetBuilder.toString()));
        } else {
            StringBuilder resBuilder = new StringBuilder();
            for (String r : resourceDirs) {
                resBuilder.append(" -S ");
                resBuilder.append(r);
            }
            if (mIsLibrary) {
                mTarget.runCommand(String.format(AAPT_PACKAGE_LIB_COMMAND, buildToolsDir, mOutput, mManifest, mIntermediateJava, mIntermediateResources, mIntermediateResources, mIntermediateResources, platform, resBuilder.toString()));
            } else {
                mTarget.runCommand(String.format(AAPT_PACKAGE_COMMAND, buildToolsDir, unalignedApk, mManifest, mIntermediateJava, mIntermediateResources, mIntermediateResources, platform, resBuilder.toString()));
            }
        }

        // Add to the build
        mMatch.addDirectory(new File(matchDir, mIntermediateJava));

        if (!sources.isEmpty()) {
            // Compile Java
            mTarget.runCommand(String.format(JAVAC_COMMAND, platform, javacClasspath, mIntermediateJava, Utilities.join(" ", sources), mIntermediateClasses));
        }

        // Add to the build
        mMatch.addDirectory(new File(matchDir, mIntermediateClasses));

        // Package jar
        mTarget.runCommand(String.format(JAR_COMMAND, mOutputJar, mIntermediateClasses, "."));

        System.out.println("Android Jar " + mOutputJar);
        mMatch.provideFile(mOutputJarFile);

        if (mIsLibrary && !mUseAapt2) {
            String classesJar = String.format("%sclasses.jar", mIntermediateJars);

            // Copy mOutputJar to classes.jar
            mTarget.runCommand(String.format(COPY_COMMAND, mOutputJar, classesJar));

            // Add classes.jar
            mTarget.runCommand(String.format(ZIP_ADD_COMMAND, mOutput, classesJar));
            System.out.println("Android AAR " + mOutput);
        } else {
            String classesDex = String.format("%sclasses.dex", mIntermediateDex);

            // Create classes.dex
            mTarget.runCommand(String.format(DX_COMMAND, buildToolsDir, classesDex, Utilities.join(" ", libraries), mIntermediateClasses));

            // Add classes.dex
            mTarget.runCommand(String.format(ZIP_ADD_COMMAND, unalignedApk, classesDex));

            // Align APK
            String alignedApk = String.format("%s%s.aligned.apk", mIntermediateApks, mName);
            mTarget.runCommand(String.format(ZIP_ALIGN_COMMAND, buildToolsDir, unalignedApk, alignedApk));

            // Sign APK
            mTarget.runCommand(String.format(APK_SIGNER_COMMAND, buildToolsDir, mKeyStore, mKeyStorePassword, mOutput, alignedApk));
            System.out.println("Android APK " + mOutput);
        }
        mMatch.provideFile(mOutputFile);
        return mOutput;
    }
}