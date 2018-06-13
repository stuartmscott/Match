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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

public class JavaJarTest {

    private static final String RESOURCE = "resource/FooBar.txt";
    private static final String CLASSES_OUT = "out/java/classes/FooBar/";
    private static final String JARS_OUT = "out/java/jar/";
    private static final String JAR_OUT = "out/java/jar/FooBar.jar";
    private static final String MANIFEST_OUT = "out/java/manifest/FooBar/";
    private static final String MANIFEST_MF = "out/java/manifest/FooBar/MANIFEST.MF";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void javaJar() {
        final String FOOBAR = "FooBar";
        IMatch match = Mockito.mock(IMatch.class);
        ITarget target = Mockito.mock(ITarget.class);
        Mockito.when(target.getDirectory()).thenReturn(folder.getRoot());
        Mockito.when(target.getFile()).thenReturn(new File(folder.getRoot(), "match"));
        Map<String, IExpression> parameters = new HashMap<String, IExpression>();
        parameters.put(Function.NAME, new Literal(match, target, FOOBAR));
        parameters.put(Function.SOURCE, new Literal(match, target, FOOBAR));
        parameters.put(JavaJar.MAIN_CLASS, new Literal(match, target, FOOBAR));
        IFunction function = new JavaJar(match, target, parameters);
        function.configure();
        String jarOut = new File(folder.getRoot(), JAR_OUT).toPath().toString();
        String manifest = new File(folder.getRoot(), MANIFEST_MF).toPath().toString();
        Assert.assertEquals("Wrong resolution", jarOut, function.resolve());
        Mockito.verify(match, Mockito.times(1)).setProperty(Mockito.eq(FOOBAR), Mockito.eq(jarOut));
        Mockito.verify(match, Mockito.times(1)).addFile(Mockito.eq(jarOut));
        Mockito.verify(target, Mockito.times(1)).runCommand(Mockito.eq(String.format(JavaJar.MKDIR_COMMAND, CLASSES_OUT)));
        Mockito.verify(target, Mockito.times(1)).runCommand(Mockito.eq(String.format(JavaJar.MKDIR_COMMAND, MANIFEST_OUT)));
        Mockito.verify(target, Mockito.times(1)).runCommand(Mockito.eq(String.format(JavaJar.MKDIR_COMMAND, JARS_OUT)));
        Mockito.verify(target, Mockito.times(1)).runCommand(Mockito.eq(String.format("echo \"Manifest-Version: 1.0\nMain-Class: FooBar\n\" > %s", manifest)));
        Mockito.verify(target, Mockito.times(1)).runCommand(Mockito.eq(String.format("javac  FooBar -d %s", CLASSES_OUT)));
        Mockito.verify(target, Mockito.times(1)).runCommand(Mockito.eq(String.format("jar cfm %s %s  -C %s .", jarOut, manifest, CLASSES_OUT)));
    }

    @Test
    public void javaJarProto() {
        // TODO
    }

    @Test
    public void javaJarResource() {
        final String FOOBAR = "FooBar";
        IMatch match = Mockito.mock(IMatch.class);
        ITarget target = Mockito.mock(ITarget.class);
        Mockito.when(target.getDirectory()).thenReturn(folder.getRoot());
        Mockito.when(target.getFile()).thenReturn(new File(folder.getRoot(), "match"));
        Map<String, IExpression> parameters = new HashMap<String, IExpression>();
        parameters.put(Function.NAME, new Literal(match, target, FOOBAR));
        parameters.put(Function.SOURCE, new Literal(match, target, FOOBAR));
        parameters.put(JavaJar.MAIN_CLASS, new Literal(match, target, FOOBAR));
        parameters.put(JavaJar.RESOURCE, new Literal(match, target, RESOURCE));
        IFunction function = new JavaJar(match, target, parameters);
        function.configure();
        String jarOut = new File(folder.getRoot(), JAR_OUT).toPath().toString();
        String manifest = new File(folder.getRoot(), MANIFEST_MF).toPath().toString();
        Assert.assertEquals("Wrong resolution", jarOut, function.resolve());
        Mockito.verify(match, Mockito.times(1)).setProperty(Mockito.eq(FOOBAR), Mockito.eq(jarOut));
        Mockito.verify(match, Mockito.times(1)).addFile(Mockito.eq(jarOut));
        Mockito.verify(target, Mockito.times(1)).runCommand(Mockito.eq(String.format(JavaJar.MKDIR_COMMAND, CLASSES_OUT)));
        Mockito.verify(target, Mockito.times(1)).runCommand(Mockito.eq(String.format(JavaJar.MKDIR_COMMAND, MANIFEST_OUT)));
        Mockito.verify(target, Mockito.times(1)).runCommand(Mockito.eq(String.format(JavaJar.MKDIR_COMMAND, JARS_OUT)));
        Mockito.verify(target, Mockito.times(1)).runCommand(Mockito.eq(String.format("echo \"Manifest-Version: 1.0\nMain-Class: FooBar\n\" > %s", manifest)));
        Mockito.verify(target, Mockito.times(1)).runCommand(Mockito.eq(String.format("javac  FooBar -d %s", CLASSES_OUT)));
        Mockito.verify(target, Mockito.times(1)).runCommand(Mockito.eq(String.format("jar cfm %s %s %s -C %s .", jarOut, manifest, RESOURCE, CLASSES_OUT)));
    }

}
