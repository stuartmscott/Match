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

import expression.Expression;
import expression.IExpression;
import expression.Literal;
import main.IMatch;
import main.ITarget;
import main.Match;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class JavaJarTest {

    private static final String COMMAND = "mkdir -p {build/java/classes/FooBar,build/java/jar} && javac  FooBar -d build/java/classes/FooBar && cd build/java/classes/FooBar && jar cfm build/java/jar/FooBar.jar build/java/classes/FooBar/MANIFEST.MF build/java/classes/FooBar";
    private static final String FOOBAR = "FooBar";
    private static final String JAR = "build/java/jar/FooBar.jar";

    @Test
    public void javaJar() {
        IMatch match = Mockito.mock(IMatch.class);
        ITarget target = Mockito.mock(ITarget.class);
        Map<String, IExpression> parameters = new HashMap<String, IExpression>();
        parameters.put(Function.NAME, new Literal(match, target, FOOBAR));
        parameters.put(Function.SOURCE, new Literal(match, target, FOOBAR));
        IFunction function = new JavaJar(match, target, parameters);
        function.setUp();
        Assert.assertEquals("Wrong resolution", JAR, function.resolve());
        Mockito.verify(match, Mockito.times(1)).runCommand(Mockito.eq(COMMAND));
    }

}
