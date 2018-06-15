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

public class JavaJar extends Function {

    public static final String ECHO_COMMAND = "echo \"Manifest-Version: 1.0\nMain-Class: %s\n%s\" > %s";
    public static final String JAR_COMMAND = "jar cfm %s %s %s -C %s .";
    public static final String JAVAC_COMMAND = "javac %s %s -d %s";
    public static final String MAIN_CLASS = "main-class";
    public static final String PROTO = "proto";
    public static final String PROTO_LITE = "proto-lite";
    public static final String PROTOC_COMMAND = "protoc --proto_path=%s --java_out=%s %s";
    // This assumes protoc-gen-javalite and protoc share directory
    public static final String PROTOC_LITE_COMMAND = "protoc --plugin=$(dirname $(which protoc))/protoc-gen-javalite --proto_path=%s --javalite_out=%s %s";

    private IExpression mSource;
    private IExpression mProtoSource;
    private IExpression mResource;
    private IExpression mMainClass;
    private String mName;
    private File mManifestFile;
    private String mManifest;
    private String mIntermediateClasses;
    private String mIntermediateManifests;
    private String mIntermediateProtos;
    private String mOutput;
    private File mOutputFile;

    public JavaJar(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        IExpression name = getParameter(NAME);
        if (!(name instanceof Literal)) {
            mMatch.error("JavaJar function expects a String name");
        }
        mName = name.resolve();
        target.setName(mName);
        mSource = getParameter(SOURCE);
        mOutputFile = new File(target.getDirectory(), JAR_OUTPUT + mName + ".jar");
        mOutput = mOutputFile.toPath().normalize().toAbsolutePath().toString();
        mIntermediateClasses = String.format("%s%s/", CLASS_OUTPUT, mName);
        if (hasParameter(PROTO)) {
            mProtoSource = getParameter(PROTO);
            mIntermediateProtos = String.format("%s%s/", PROTO_OUTPUT, mName);
        }
        if (hasParameter(RESOURCE)) {
            mResource = getParameter(RESOURCE);
        }
        mIntermediateManifests = String.format("%s%s/", MANIFEST_OUTPUT, mName);
        mManifestFile = new File(target.getDirectory(), mIntermediateManifests + "MANIFEST.MF");
        mManifest = mManifestFile.toPath().normalize().toAbsolutePath().toString();
        mMainClass = getParameter(MAIN_CLASS);
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
        File matchDir = mTarget.getDirectory();
        List<String> libraries = new ArrayList<String>();
        String javacClasspath = "";
        String jarClasspath = "";
        // TODO consider extract libraries into intermediate classes directory
        if (hasParameter(LIBRARY)) {
            for (String library : getParameter(LIBRARY).resolveList()) {
                String path = mMatch.getProperty(library);
                mMatch.awaitFile(path);
                libraries.add(path);
            }
            // TODO this is broken:
            // error: java.io.IOException: line too long
            // error:  at java.util.jar.Attributes.read(Attributes.java:379)
            // error:  at java.util.jar.Manifest.read(Manifest.java:199)
            // error:  at java.util.jar.Manifest.<init>(Manifest.java:69)
            // error:  at sun.tools.jar.Main.run(Main.java:176)
            // error:  at sun.tools.jar.Main.main(Main.java:1288)
            // error: jar cfm out/java/jar/JoyTest.jar out/java/manifest/JoyTest/MANIFEST.MF  -C out/java/classes/JoyTest/ .
            // TODO maybe extract libraries into intermediate directory then jar together
            javacClasspath = String.format("-cp %s", Utilities.join(":", libraries));
            //jarClasspath = String.format("Class-Path: %s\n", Utilities.join(":", libraries));
        }
        mTarget.runCommand(String.format(MKDIR_COMMAND, mIntermediateClasses));
        mTarget.runCommand(String.format(MKDIR_COMMAND, mIntermediateManifests));
        mTarget.runCommand(String.format(MKDIR_COMMAND, JAR_OUTPUT));
        mTarget.runCommand(String.format(ECHO_COMMAND, mMainClass.resolve(), jarClasspath, mManifest));
        mMatch.provideFile(mManifestFile);
        Set<String> sources = new HashSet<String>();
        sources.addAll(mSource.resolveList());
        // Compile protos
        if (mProtoSource != null) {
            mTarget.runCommand(String.format(MKDIR_COMMAND, mIntermediateProtos));
            if (hasParameter(PROTO_LITE) && getParameter(PROTO_LITE).resolve().equals("true")) {
                mTarget.runCommand(String.format(PROTOC_LITE_COMMAND, matchDir, mIntermediateProtos, Utilities.join(" ", mProtoSource.resolveList())));
            } else {
                mTarget.runCommand(String.format(PROTOC_COMMAND, matchDir, mIntermediateProtos, Utilities.join(" ", mProtoSource.resolveList())));
            }
            File directory = new File(matchDir, mIntermediateProtos);
            // Add to the build
            mMatch.addDirectory(directory);
            // Get the relative paths of all java files generated
            String path = directory.toPath().toString() + "/";
            Set<String> generated = new HashSet<String>();
            Find.scanFiles(directory, path, generated, Pattern.compile(".*.java"));
            sources.addAll(generated);
        }
        // Compile java
        mTarget.runCommand(String.format(JAVAC_COMMAND, javacClasspath, Utilities.join(" ", sources), mIntermediateClasses));
        // Add to the build
        mMatch.addDirectory(new File(matchDir, mIntermediateClasses));
        // Package jar
        String resources = (mResource == null) ? "" : Utilities.join(" ", mResource.resolveList());
        mTarget.runCommand(String.format(JAR_COMMAND, mOutput, mManifest, resources, mIntermediateClasses));
        mMatch.provideFile(mOutputFile);
        return mOutput;
    }
}
