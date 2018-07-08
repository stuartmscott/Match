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
import java.nio.file.Files;
import java.nio.file.Path;
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

public class AndroidApk extends Function {

    public static final String AAPT_PACKAGE_COMMAND = "%saapt package -v -m -f -F %s -M %s -J %s --output-text-symbols %s -P %s/public_resources.xml -G %s/proguard.txt -I %s %s";
    public static final String AAPT_PACKAGE_LIB_COMMAND = "%saapt package -v -m -f -F %s -M %s -J %s --non-constant-id --output-text-symbols %s -P %s/public_resources.xml -G %s/proguard.txt -I %s %s";
    public static final String AAPT2_COMPILE_COMMAND = "%saapt2 compile -o %s %s";
    public static final String AAPT2_LINK_COMMAND = "%saapt2 link --auto-add-overlay --no-static-lib-packages -o %s --manifest %s --java %s -I %s %s %s %s";
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
    public static final String EXTRA_PACKAGE = "extra-package";
    public static final String FIND_RM_COMMAND = "find %s -name %s -print0 | xargs -0 rm";
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

    private IExpression source;
    private IExpression protoSource;
    private IExpression assetDirectories;
    private IExpression resourceDirectories;
    private IExpression extraPackages;
    private boolean isLibrary = false;
    private boolean useAapt2 = false;
    private String name;
    private String apiVersion;
    private String buildToolsVersion;
    private String keyStore;
    private String keyStorePassword;
    private String manifest;
    private String intermediateAars;
    private String intermediateApks;
    private String intermediateClasses;
    private String intermediateDex;
    private String intermediateJars;
    private String intermediateJava;
    private String intermediateProtos;
    private String intermediateResources;
    private String output;
    private File outputFile;
    private String outputJar;
    private File outputJarFile;

    /**
     * Initializes the function with the given parameters.
     */
    public AndroidApk(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        IExpression n = getParameter(NAME);
        if (!(n instanceof Literal)) {
            match.error("AndroidApk function expects a String name");
        }
        name = n.resolve();
        target.setName(name);
        source = getParameter(SOURCE);
        apiVersion = getParameter(API_VERSION).resolve();
        buildToolsVersion = getParameter(BUILD_TOOLS_VERSION).resolve();
        keyStore = getParameter(KEYSTORE).resolve();
        keyStorePassword = getParameter(KEYSTORE_PASSWORD_FILE).resolve();
        intermediateAars = String.format("%s%s/", AAR_OUTPUT, name);
        intermediateApks = String.format("%s%s/", APK_OUTPUT, name);
        intermediateClasses = String.format("%s%s/", CLASS_OUTPUT, name);
        intermediateDex = String.format("%s%s/", DEX_OUTPUT, name);
        intermediateJars = String.format("%s%s/", JAR_OUTPUT, name);
        intermediateJava = String.format("%s%s/", JAVA_OUTPUT, name);
        intermediateResources = String.format("%s%s/", RESOURCE_OUTPUT, name);
        if (hasParameter(MANIFEST)) {
            IExpression m = getParameter(MANIFEST);
            if (!(m instanceof Literal)) {
                match.error("AndroidApk function expects a String manifest");
            }
            manifest = m.resolve();
        } else {
            manifest = "AndroidManifest.xml";
        }
        if (hasParameter(PROTO)) {
            protoSource = getParameter(PROTO);
            intermediateProtos = String.format("%s%s/", PROTO_OUTPUT, name);
        }
        if (hasParameter(ASSET_DIRECTORY)) {
            assetDirectories = getParameter(ASSET_DIRECTORY);
        }
        if (hasParameter(RESOURCE_DIRECTORY)) {
            resourceDirectories = getParameter(RESOURCE_DIRECTORY);
        }
        if (hasParameter(EXTRA_PACKAGE)) {
            extraPackages = getParameter(EXTRA_PACKAGE);
        }
        if (hasParameter(IS_LIBRARY)) {
            isLibrary = Boolean.parseBoolean(getParameter(IS_LIBRARY).resolve());
        }
        if (hasParameter(USE_AAPT2)) {
            useAapt2 = Boolean.parseBoolean(getParameter(USE_AAPT2).resolve());
        }
        if (isLibrary && !useAapt2) {
            outputFile = new File(target.getDirectory(), intermediateAars + name + ".aar");
            output = outputFile.toPath().normalize().toAbsolutePath().toString();
        } else {
            outputFile = new File(target.getDirectory(), intermediateApks + name + ".apk");
            output = outputFile.toPath().normalize().toAbsolutePath().toString();
        }
        outputJarFile = new File(target.getDirectory(), intermediateJars + name + ".jar");
        outputJar = outputJarFile.toPath().normalize().toAbsolutePath().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        match.addFile(output);
        match.setProperty(name, output);
        match.addFile(outputJar);
        match.setProperty(name + "-jar", outputJar);
        source.configure();
        if (protoSource != null) {
            protoSource.configure();
        }
        if (assetDirectories != null) {
            assetDirectories.configure();
        }
        if (resourceDirectories != null) {
            resourceDirectories.configure();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        File matchDir = target.getFile().getParentFile();
        String sdkLocation = match.getProperty(SDK_LOCATION);
        String buildToolsDir = String.format(BUILD_TOOLS, sdkLocation, buildToolsVersion);
        String platform = String.format(PLATFORM, sdkLocation, apiVersion);
        String unalignedApk = String.format("%s%s.unaligned.apk", intermediateApks, name);
        Set<String> assetDirs = new HashSet<>();
        if (assetDirectories != null) {
            assetDirs.addAll(assetDirectories.resolveList());
        }
        Set<String> libraries = new HashSet<>();
        List<String> resources = new ArrayList<>();// Resources need to be ordered overlay first
        Set<String> resourceDirs = new HashSet<>();
        if (resourceDirectories != null) {
            resourceDirs.addAll(resourceDirectories.resolveList());
        }
        Set<String> sources = new HashSet<>();
        sources.addAll(source.resolveList());
        String javacClasspath = "";
        if (hasParameter(LIBRARY)) {
            for (String library : getParameter(LIBRARY).resolveList()) {
                String path = match.getProperty(library);
                match.awaitFile(path);
                if (path.endsWith(".aar")) {
                    // Extract classes.jar to temp file
                    try {
                        Path p = Files.createTempDirectory(library);
                        File dir = p.toFile();
                        dir.deleteOnExit();
                        target.runCommand(String.format(UNZIP_FILE_COMMAND, path, CLASSES_JAR, p.normalize().toAbsolutePath().toString()));
                        File classesJar = new File(dir, CLASSES_JAR);
                        if (classesJar.exists()) {
                            libraries.add(classesJar.toPath().normalize().toAbsolutePath().toString());
                        }
                    } catch (Exception e) {
                        match.error(e);
                    }
                } else if (path.endsWith(".apk")) {
                    // Add to the resources
                    resources.add(path);
                    // Get the libraries classes.jar
                    String jar = match.getProperty(library + "-jar");
                    match.awaitFile(jar);
                    libraries.add(jar);
                } else if (path.endsWith(".jar")) {
                    libraries.add(path);
                } else {
                    match.error("AndroidApk encountered unknown library type: " + path);
                }
            }
            javacClasspath = String.format("-cp %s", Utilities.join(":", libraries));
        }
        target.runCommand(String.format(MKDIR_COMMAND, intermediateClasses));
        target.runCommand(String.format(MKDIR_COMMAND, intermediateJars));
        target.runCommand(String.format(MKDIR_COMMAND, intermediateJava));
        target.runCommand(String.format(MKDIR_COMMAND, intermediateResources));

        // Compile protos
        if (protoSource != null) {
            /* TODO
            File dir = target.getFile().getParentFile();
            target.runCommand(String.format(MKDIR_COMMAND, intermediateProtos));
            if (hasParameter(PROTO_LITE) && getParameter(PROTO_LITE).resolve().equals("true")) {
                target.runCommand(String.format(PROTOC_LITE_COMMAND, dir, intermediateProtos, Utilities.join(" ", protoSource.resolveList())));
            } else {
                target.runCommand(String.format(PROTOC_COMMAND, dir, intermediateProtos, Utilities.join(" ", protoSource.resolveList())));
            }
            File directory = new File(intermediateProtos);
            // Add to the build
            match.addDirectory(directory);
            // Get the relative paths of all java files generated
            String path = directory.toPath().toString() + "/";
            Set<String> generated = new HashSet<>();
            Find.scanFiles(directory, path, generated, Pattern.compile(".*.java"));
            sources.addAll(generated);
            */
        }

        if (useAapt2) {
            // Find all resource files
            Set<String> resourceFiles = new HashSet<>();
            for (String dir : resourceDirs) {
                Find.scanFiles(new File(matchDir, dir), dir + "/", resourceFiles, ".*");
            }
            // Compile resources
            for (String f : resourceFiles) {
                target.runCommand(String.format(AAPT2_COMPILE_COMMAND, buildToolsDir, intermediateResources, f));
            }

            // Add to the build
            match.addDirectory(new File(matchDir, intermediateResources));

            // Link APK
            StringBuilder resBuilder = new StringBuilder();
            Find.scanFiles(new File(matchDir, intermediateResources), intermediateResources, resources, ".*");
            for (String r : resources) {
                resBuilder.append(" -R ");
                resBuilder.append(r);
            }
            StringBuilder assetBuilder = new StringBuilder();
            for (String a : assetDirs) {
                assetBuilder.append(" -A ");
                assetBuilder.append(a);
            }
            StringBuilder extraPackagesBuilder = new StringBuilder();
            if (extraPackages != null) {
                extraPackagesBuilder.append(" --extra-packages ");
                for (String p : extraPackages.resolveList()) {
                    extraPackagesBuilder.append(p);
                    extraPackagesBuilder.append(" ");
                }
            }
            target.runCommand(String.format(MKDIR_COMMAND, intermediateApks));
            if (isLibrary) {
                target.runCommand(String.format(AAPT2_LINK_LIB_COMMAND, buildToolsDir, unalignedApk, manifest, intermediateJava, platform, resBuilder.toString(), assetBuilder.toString()));
            } else {
                target.runCommand(String.format(AAPT2_LINK_COMMAND, buildToolsDir, unalignedApk, manifest, intermediateJava, platform, resBuilder.toString(), assetBuilder.toString(), extraPackagesBuilder.toString()));
            }
        } else {
            StringBuilder resBuilder = new StringBuilder();
            for (String r : resourceDirs) {
                resBuilder.append(" -S ");
                resBuilder.append(r);
            }
            if (isLibrary) {
                target.runCommand(String.format(MKDIR_COMMAND, intermediateAars));
                target.runCommand(String.format(AAPT_PACKAGE_LIB_COMMAND, buildToolsDir, output, manifest, intermediateJava, intermediateResources, intermediateResources, intermediateResources, platform, resBuilder.toString()));
            } else {
                target.runCommand(String.format(MKDIR_COMMAND, intermediateApks));
                target.runCommand(String.format(AAPT_PACKAGE_COMMAND, buildToolsDir, unalignedApk, manifest, intermediateJava, intermediateResources, intermediateResources, intermediateResources, platform, resBuilder.toString()));
            }
        }

        // Add to the build
        match.addDirectory(new File(matchDir, intermediateJava));

        if (!sources.isEmpty()) {
            // Compile Java
            target.runCommand(String.format(JAVAC_COMMAND, platform, javacClasspath, intermediateJava, Utilities.join(" ", sources), intermediateClasses));
        }

        if (isLibrary && useAapt2) {
            // Delete R files so they wont cause errors with DX:
            // Uncaught translation error:
            // java.lang.IllegalArgumentException: already added: Lhello/droid/common/R;
            //    at com.android.dx.dex.file.ClassDefsSection.add(ClassDefsSection.java:122)
            // The App which links this library will generate its own R files through use of --no-static-lib-package and --extra-packages.
            target.runCommand(String.format(FIND_RM_COMMAND, intermediateClasses, "R.class"));
            target.runCommand(String.format(FIND_RM_COMMAND, intermediateClasses, "R'$'*.class"));
        }

        // Add to the build
        match.addDirectory(new File(matchDir, intermediateClasses));

        // Package jar
        target.runCommand(String.format(JAR_COMMAND, outputJar, intermediateClasses, "."));

        System.out.println("Android Jar " + outputJar);
        match.provideFile(outputJarFile);

        if (isLibrary && !useAapt2) {
            String classesJar = String.format("%sclasses.jar", intermediateJars);

            // Copy outputJar to classes.jar
            target.runCommand(String.format(COPY_COMMAND, outputJar, classesJar));

            // Add classes.jar
            target.runCommand(String.format(ZIP_ADD_COMMAND, output, classesJar));
            System.out.println("Android AAR " + output);
        } else {
            target.runCommand(String.format(MKDIR_COMMAND, intermediateDex));

            String classesDex = String.format("%sclasses.dex", intermediateDex);

            // Create classes.dex
            target.runCommand(String.format(DX_COMMAND, buildToolsDir, classesDex, Utilities.join(" ", libraries), intermediateClasses));

            // Add classes.dex
            target.runCommand(String.format(ZIP_ADD_COMMAND, unalignedApk, classesDex));

            // Align APK
            String alignedApk = String.format("%s%s.aligned.apk", intermediateApks, name);
            target.runCommand(String.format(ZIP_ALIGN_COMMAND, buildToolsDir, unalignedApk, alignedApk));

            // Sign APK
            target.runCommand(String.format(APK_SIGNER_COMMAND, buildToolsDir, keyStore, keyStorePassword, output, alignedApk));
            System.out.println("Android APK " + output);
        }
        match.provideFile(outputFile);
        return output;
    }
}