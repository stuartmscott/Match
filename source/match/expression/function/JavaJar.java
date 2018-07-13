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
import java.util.regex.Pattern;

import match.IMatch;
import match.ITarget;
import match.Utilities;
import match.expression.IExpression;
import match.expression.Literal;

/**
 * A function to compile Java source code and package into a jar file.
 */
public class JavaJar extends Function {

    public static final String ECHO_COMMAND = "echo \"Manifest-Version: 1.0\nMain-Class: %s\" > %s";
    public static final String JAR_COMMAND = "jar cfm %s %s %s -C %s .";
    public static final String JAVAC_COMMAND = "javac %s %s -d %s";
    public static final String MAIN_CLASS = "main-class";

    private IExpression source;
    private IExpression resource;
    private IExpression mainClass;
    private String name;
    private File manifestFile;
    private String manifest;
    private String intermediateClasses;
    private String intermediateManifests;
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
        if (resource != null) {
            resource.configure();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
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
        // Compile java
        target.runCommand(String.format(JAVAC_COMMAND, javacClasspath, Utilities.join(" ", source.resolveList()), intermediateClasses));
        // Add to the build
        match.addDirectory(new File(target.getDirectory(), intermediateClasses));
        // Package jar
        String resources = (resource == null) ? "" : Utilities.join(" ", resource.resolveList());
        target.runCommand(String.format(JAR_COMMAND, output, manifest, resources, intermediateClasses));
        match.provideFile(outputFile);
        return output;
    }
}
