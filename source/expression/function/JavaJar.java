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

    private static final String ECHO_COMMAND = "echo \"Manifest-Version: 1.0\nMain-Class: %s\n%s\" > %s";
    private static final String JAVAC_COMMAND = "javac %s %s -d %s";
    private static final String JAR_COMMAND = "jar cfm %s %s -C %s .";
    private static final String MKDIR_COMMAND = "mkdir -p %s";
    private static final String PROTO = "proto";
    private static final String PROTO_LITE = "proto_lite";
    private static final String PROTOC_COMMAND = "protoc --java_out=%s %s";
    // This assumes protoc-gen-javalite and protoc share directory
    private static final String PROTOC_LITE_COMMAND = "protoc --plugin=$(dirname $(which protoc))/protoc-gen-javalite --javalite_out=%s %s";
    private static final String RESOURCE = "resource";

    private IExpression mSource;
    private IExpression mProtoSource;
    private IExpression mResource;
    private IExpression mMainClass;
    private String mName;
    private String mManifest;
    private String mIntermediateClasses;
    private String mIntermediateManifests;
    private String mIntermediateProtos;
    private String mOutput;

    public JavaJar(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        IExpression name = getParameter(NAME);
        if (!(name instanceof Literal)) {
            mMatch.error("JavaJar function expects a String name");
        }
        mName = name.resolve();
        target.setName(mName);
        mSource = getParameter(SOURCE);
        mOutput = JAR_OUTPUT + mName + ".jar";
        mIntermediateClasses = String.format("%s%s/", CLASS_OUTPUT, mName);
        if (hasParameter(PROTO)) {
            mProtoSource = getParameter(PROTO);
            mIntermediateProtos = String.format("%s%s/", PROTO_OUTPUT, mName);
        }
        if (hasParameter(RESOURCE)) {
            mResource = getParameter(RESOURCE);
        }
        mIntermediateManifests = String.format("%s%s/", MANIFEST_OUTPUT, mName);
        mManifest = mIntermediateManifests + "MANIFEST.MF";
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
        List<String> libraries = new ArrayList<String>();
        String javacClasspath = "";
        String jarClasspath = "";
        if (hasParameter(LIBRARY)) {
            for (String library : getParameter(LIBRARY).resolveList()) {
                String path = mMatch.getProperty(library);
                mMatch.awaitFile(path);
                libraries.add(path);
            }
            javacClasspath = String.format("-cp %s", Utilities.join(":", libraries));
            jarClasspath = String.format("Class-Path: %s\n", Utilities.join(":", libraries));
        }
        mMatch.runCommand(String.format(MKDIR_COMMAND, mIntermediateClasses));
        mMatch.runCommand(String.format(MKDIR_COMMAND, mIntermediateManifests));
        mMatch.runCommand(String.format(MKDIR_COMMAND, JAR_OUTPUT));
        mMatch.runCommand(String.format(ECHO_COMMAND, mMainClass.resolve(), jarClasspath, mManifest));
        mMatch.provideFile(mManifest);
        Set<String> sources = new HashSet<String>();
        sources.addAll(mSource.resolveList());
        // Compile protos
        if (mProtoSource != null) {
            mMatch.runCommand(String.format(MKDIR_COMMAND, mIntermediateProtos));
            if (hasParameter(PROTO_LITE) && getParameter(PROTO_LITE).resolve().equals("true")) {
                mMatch.runCommand(String.format(PROTOC_LITE_COMMAND, mIntermediateProtos, Utilities.join(" ", mProtoSource.resolveList())));
            } else {
                mMatch.runCommand(String.format(PROTOC_COMMAND, mIntermediateProtos, Utilities.join(" ", mProtoSource.resolveList())));
            }
            File directory = new File(mIntermediateProtos);
            // Add to the build
            mMatch.addDirectory(directory);
            // Get the relative paths of all java files generated
            String path = directory.getAbsolutePath() + "/";
            Set<String> generated = new HashSet<String>();
            Find.scanFiles(directory, path, generated, Pattern.compile(".*.java"));
            sources.addAll(generated);
        }
        // Compile java
        mMatch.runCommand(String.format(JAVAC_COMMAND, javacClasspath, Utilities.join(" ", sources), mIntermediateClasses));
        // Add to the build
        mMatch.addDirectory(new File(mIntermediateClasses));
        // Package jar
        Set<String> jarIn = new HashSet<String>();
        jarIn.add(mIntermediateClasses);
        if (mResource != null) {
            jarIn.addAll(mResource.resolveList());
        }
        mMatch.runCommand(String.format(JAR_COMMAND, mOutput, mManifest, Utilities.join(",", jarIn)));
        mMatch.provideFile(mOutput);
        return mOutput;
    }
}
