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
import main.IMatch;
import main.ITarget;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class JavaJarTest {

    private static final String CLASSES_OUT = "./out/java/classes/FooBar";
    private static final String JARS_OUT = "./out/java/jar";
    private static final String JAR_OUT = "./out/java/jar/FooBar.jar";
    private static final String MANIFEST_OUT = "./out/java/classes/FooBar/MANIFEST.MF";
    private static final String MKDIR_COMMAND = String.format("mkdir -p {%s,%s}", CLASSES_OUT, JARS_OUT);
    private static final String ECHO_COMMAND = String.format("echo \"Manifest-Version: 1.0\nMain-Class: FooBar\n\" > %s", MANIFEST_OUT);
    private static final String JAVAC_COMMAND = String.format("javac  FooBar -d %s", CLASSES_OUT);
    private static final String JAR_COMMAND = String.format("jar cfm %s %s -C %s .", JAR_OUT, MANIFEST_OUT, CLASSES_OUT);

    @Test
    public void javaJar() {
        final String FOOBAR = "FooBar";
        IMatch match = Mockito.mock(IMatch.class);
        ITarget target = Mockito.mock(ITarget.class);
        Map<String, IExpression> parameters = new HashMap<String, IExpression>();
        parameters.put(Function.NAME, new Literal(match, target, FOOBAR));
        parameters.put(Function.SOURCE, new Literal(match, target, FOOBAR));
        parameters.put(Function.MAIN_CLASS, new Literal(match, target, FOOBAR));
        IFunction function = new JavaJar(match, target, parameters);
        function.configure();
        Assert.assertEquals("Wrong resolution", JAR_OUT, function.resolve());
        Mockito.verify(match, Mockito.times(1)).runCommand(Mockito.eq(MKDIR_COMMAND));
        Mockito.verify(match, Mockito.times(1)).runCommand(Mockito.eq(ECHO_COMMAND));
        Mockito.verify(match, Mockito.times(1)).runCommand(Mockito.eq(JAVAC_COMMAND));
        Mockito.verify(match, Mockito.times(1)).runCommand(Mockito.eq(JAR_COMMAND));
    }

}
