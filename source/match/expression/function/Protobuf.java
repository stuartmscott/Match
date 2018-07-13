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

package match.expression.function;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import match.IMatch;
import match.ITarget;
import match.Utilities;
import match.expression.IExpression;
import match.expression.Literal;

/**
 * A function to build Protocol Buffers into Java.
 */
public class Protobuf extends Function {

    public static final String JAR_COMMAND = "jar cf %s -C %s .";
    public static final String JAVAC_COMMAND = "javac -cp %s %s -d %s";
    public static final String PROTOC_COMMAND = "protoc --proto_path=%s --java_out=%s %s";
    // This assumes protoc-gen-javalite and protoc share directory
    public static final String PROTOC_LITE_COMMAND = "protoc --plugin=$(dirname $(which protoc))/protoc-gen-javalite --proto_path=%s --javalite_out=%s %s";

    private final IExpression source;
    private final String name;
    private final String intermediateClasses;
    private final String intermediateJava;
    private final boolean isLite;
    private final String output;
    private final File outputFile;

    /**
     * Initializes the function with the given parameters.
     */
    public Protobuf(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        IExpression n = getParameter(NAME);
        if (!(n instanceof Literal)) {
            match.error("Protobuf function expects a String name");
        }
        name = n.resolve();
        target.setName(name);
        source = getParameter(SOURCE);
        intermediateClasses = String.format("%s%s/", CLASS_OUTPUT, name);
        intermediateJava = String.format("%s%s/", JAVA_OUTPUT, name);
        isLite = hasParameter(PROTO_LITE) && getParameter(PROTO_LITE).resolve().equals("true");
        outputFile = new File(target.getDirectory(), JAR_OUTPUT + name + ".jar");
        output = outputFile.toPath().normalize().toAbsolutePath().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        match.addFile(output);
        match.setProperty(name, output);
        source.configure();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        File matchDir = target.getDirectory();
        target.runCommand(String.format(MKDIR_COMMAND, intermediateClasses));
        target.runCommand(String.format(MKDIR_COMMAND, intermediateJava));
        target.runCommand(String.format(MKDIR_COMMAND, JAR_OUTPUT));
        // Compile protos
        if (isLite) {
            target.runCommand(String.format(PROTOC_LITE_COMMAND, matchDir, intermediateJava, Utilities.join(" ", source.resolveList())));
        } else {
            target.runCommand(String.format(PROTOC_COMMAND, matchDir, intermediateJava, Utilities.join(" ", source.resolveList())));
        }
        File directory = new File(matchDir, intermediateJava);
        // Add to the build
        match.addDirectory(directory);
        // Get the relative paths of all java files generated
        String path = directory.toPath().toString() + "/";
        Set<String> sources = new HashSet<String>();
        Find.scanFiles(directory, path, sources, ".*.java");
        // Get library
        String library = match.getProperty(isLite ? "protobuf-lite" : "protobuf");
        match.awaitFile(library);
        // Compile java
        target.runCommand(String.format(JAVAC_COMMAND, library, Utilities.join(" ", sources), intermediateClasses));
        // Add to the build
        match.addDirectory(new File(matchDir, intermediateClasses));
        // Package jar
        target.runCommand(String.format(JAR_COMMAND, output, intermediateClasses));
        match.provideFile(outputFile);
        return output;
    }
}
