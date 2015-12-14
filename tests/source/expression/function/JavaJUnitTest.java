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

import expression.ExpressionList;
import expression.IExpression;
import expression.Literal;
import main.IMatch;
import main.ITarget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class JavaJUnitTest {

    private static final String MKDIR_COMMAND = "mkdir -p out/results";
    private static final String RUN_COMMAND = "java -cp out/java/jar/FooBar.jar:out/java/jar/FooBarTest.jar:libraries/junit.jar:libraries/hamcrest.jar:libraries/mockito.jar org.junit.runner.JUnitCore main.AllTests > out/results/FooBarTestResult";
    private static final String FOOBAR = "FooBar";
    private static final String FOOBAR_MAIN_CLASS = "main.AllTests";
    private static final String FOOBAR_RESULT = "FooBarTestResult";
    private static final String FOOBAR_TEST = "FooBarTest";
    private static final String OUTPUT = "out/results/FooBarTestResult";

    @Test
    public void javaJUnit() {
        IMatch match = Mockito.mock(IMatch.class);
        Mockito.when(match.getProperty("junit")).thenReturn("libraries/junit.jar");
        Mockito.when(match.getProperty("hamcrest")).thenReturn("libraries/hamcrest.jar");
        Mockito.when(match.getProperty("mockito")).thenReturn("libraries/mockito.jar");
        Mockito.when(match.getProperty("FooBar")).thenReturn("out/java/jar/FooBar.jar");
        Mockito.when(match.getProperty("FooBarTest")).thenReturn("out/java/jar/FooBarTest.jar");
        ITarget target = Mockito.mock(ITarget.class);
        Map<String, IExpression> parameters = new HashMap<String, IExpression>();
        parameters.put(Function.NAME, new Literal(match, target, FOOBAR_RESULT));
        List<IExpression> elements = new ArrayList<>();
        elements.add(new Literal(match, target, FOOBAR));
        elements.add(new Literal(match, target, FOOBAR_TEST));
		parameters.put(Function.LIBRARY, new ExpressionList(match, target, elements));
        parameters.put(Function.MAIN_CLASS, new Literal(match, target, FOOBAR_MAIN_CLASS));
        IFunction function = new JavaJUnit(match, target, parameters);
        function.configure();
        Assert.assertEquals("Wrong resolution", OUTPUT, function.resolve());
        Mockito.verify(match, Mockito.times(1)).runCommand(Mockito.eq(MKDIR_COMMAND));
        Mockito.verify(match, Mockito.times(1)).runCommand(Mockito.eq(RUN_COMMAND));
    }

}
