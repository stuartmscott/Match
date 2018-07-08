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

public class JavaJar extends Function {

    public static final String ECHO_COMMAND = "echo \"Manifest-Version: 1.0\nMain-Class: %s\" > %s";
    public static final String JAR_COMMAND = "jar cfm %s %s %s -C %s .";
    public static final String JAVAC_COMMAND = "javac %s %s -d %s";
    public static final String MAIN_CLASS = "main-class";
    public static final String PROTOC_COMMAND = "protoc --proto_path=%s --java_out=%s %s";
    // This assumes protoc-gen-javalite and protoc share directory
    public static final String PROTOC_LITE_COMMAND = "protoc --plugin=$(dirname $(which protoc))/protoc-gen-javalite --proto_path=%s --javalite_out=%s %s";

    private IExpression source;
    private IExpression protoSource;
    private IExpression resource;
    private IExpression mainClass;
    private String name;
    private File manifestFile;
    private String manifest;
    private String intermediateClasses;
    private String intermediateManifests;
    private String intermediateProtos;
    private String output;
    private File outputFile;

    /**
     * Initializes the function with the given parameters.
     */
    public JavaJar(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        IExpression n = getParameter(NAME);
        if (!(n instanceof Literal)) {
            match.error("JavaJar function expects a String name");
        }
        name = n.resolve();
        target.setName(name);
        source = getParameter(SOURCE);
        outputFile = new File(target.getDirectory(), JAR_OUTPUT + name + ".jar");
        output = outputFile.toPath().normalize().toAbsolutePath().toString();
        intermediateClasses = String.format("%s%s/", CLASS_OUTPUT, name);
        if (hasParameter(PROTO)) {
            protoSource = getParameter(PROTO);
            intermediateProtos = String.format("%s%s/", PROTO_OUTPUT, name);
        }
        if (hasParameter(RESOURCE)) {
            resource = getParameter(RESOURCE);
        }
        intermediateManifests = String.format("%s%s/", MANIFEST_OUTPUT, name);
        manifestFile = new File(target.getDirectory(), intermediateManifests + "MANIFEST.MF");
        manifest = manifestFile.toPath().normalize().toAbsolutePath().toString();
        mainClass = getParameter(MAIN_CLASS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        match.addFile(manifest);
        match.addFile(output);
        match.setProperty(name, output);
        source.configure();
        if (protoSource != null) {
            protoSource.configure();
        }
        if (resource != null) {
            resource.configure();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        File matchDir = target.getDirectory();
        List<String> libraries = new ArrayList<String>();
        String javacClasspath = "";
        // TODO consider extract libraries into intermediate classes directory
        if (hasParameter(LIBRARY)) {
            for (String library : getParameter(LIBRARY).resolveList()) {
                String path = match.getProperty(library);
                match.awaitFile(path);
                libraries.add(path);
            }
            javacClasspath = String.format("-cp %s", Utilities.join(":", libraries));
        }
        target.runCommand(String.format(MKDIR_COMMAND, intermediateClasses));
        target.runCommand(String.format(MKDIR_COMMAND, intermediateManifests));
        target.runCommand(String.format(MKDIR_COMMAND, JAR_OUTPUT));
        target.runCommand(String.format(ECHO_COMMAND, mainClass.resolve(), manifest));
        match.provideFile(manifestFile);
        Set<String> sources = new HashSet<String>();
        sources.addAll(source.resolveList());
        // Compile protos
        if (protoSource != null) {
            target.runCommand(String.format(MKDIR_COMMAND, intermediateProtos));
            if (hasParameter(PROTO_LITE) && getParameter(PROTO_LITE).resolve().equals("true")) {
                target.runCommand(String.format(PROTOC_LITE_COMMAND, matchDir, intermediateProtos, Utilities.join(" ", protoSource.resolveList())));
            } else {
                target.runCommand(String.format(PROTOC_COMMAND, matchDir, intermediateProtos, Utilities.join(" ", protoSource.resolveList())));
            }
            File directory = new File(matchDir, intermediateProtos);
            // Add to the build
            match.addDirectory(directory);
            // Get the relative paths of all java files generated
            String path = directory.toPath().toString() + "/";
            Set<String> generated = new HashSet<String>();
            Find.scanFiles(directory, path, generated, Pattern.compile(".*.java"));
            sources.addAll(generated);
        }
        // Compile java
        target.runCommand(String.format(JAVAC_COMMAND, javacClasspath, Utilities.join(" ", sources), intermediateClasses));
        // Add to the build
        match.addDirectory(new File(matchDir, intermediateClasses));
        // Package jar
        String resources = (resource == null) ? "" : Utilities.join(" ", resource.resolveList());
        target.runCommand(String.format(JAR_COMMAND, output, manifest, resources, intermediateClasses));
        match.provideFile(outputFile);
        return output;
    }
}
