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
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class JavaJarTest {

    private static final String MKDIR_COMMAND = "mkdir -p {out/java/classes/FooBar,out/java/jar}";
    private static final String ECHO_COMMAND = "echo \"Manifest-Version: 1.0\nMain-Class: FooBar\n\" > out/java/classes/FooBar/MANIFEST.MF";
    private static final String JAVAC_COMMAND = "javac  FooBar -d out/java/classes/FooBar";
    private static final String JAR_COMMAND = "jar cfm out/java/jar/FooBar.jar out/java/classes/FooBar/MANIFEST.MF -C out/java/classes/FooBar .";
    private static final String FOOBAR = "FooBar";
    private static final String JAR = "out/java/jar/FooBar.jar";

    @Test
    public void javaJar() {
        IMatch match = Mockito.mock(IMatch.class);
        ITarget target = Mockito.mock(ITarget.class);
        Map<String, IExpression> parameters = new HashMap<String, IExpression>();
        parameters.put(Function.NAME, new Literal(match, target, FOOBAR));
        parameters.put(Function.SOURCE, new Literal(match, target, FOOBAR));
        parameters.put(Function.MAIN_CLASS, new Literal(match, target, FOOBAR));
        IFunction function = new JavaJar(match, target, parameters);
        function.configure();
        Assert.assertEquals("Wrong resolution", JAR, function.resolve());
        Mockito.verify(match, Mockito.times(1)).runCommand(Mockito.eq(MKDIR_COMMAND));
        Mockito.verify(match, Mockito.times(1)).runCommand(Mockito.eq(ECHO_COMMAND));
        Mockito.verify(match, Mockito.times(1)).runCommand(Mockito.eq(JAVAC_COMMAND));
        Mockito.verify(match, Mockito.times(1)).runCommand(Mockito.eq(JAR_COMMAND));
    }

}
