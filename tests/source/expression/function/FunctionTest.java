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

import java.util.HashMap;
import java.util.Map;

import match.IMatch;
import match.ITarget;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Tests for Functions.
 */
public class FunctionTest {

    private static final String FOO = "foo";
    private static final String BAR = "bar";
    private static final String FAKE = "FunctionFake";

    @Mock
    private IMatch match;
    @Mock
    private ITarget target;
    private Map<String, IExpression> parameters;

    /**
     * Sets up the mocks and paramters for the tests.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        parameters = new HashMap<String, IExpression>();
    }

    /**
     * Tests the function can get parameters.
     */
    @Test
    public void parameters() {
        Literal literal = new Literal(match, target, BAR);
        parameters.put(FOO, literal);
        IFunction function = new FunctionFake(match, target, parameters);
        Assert.assertEquals("Wrong parameter", BAR, function.getParameter(FOO).resolve());
    }

    /**
     * Tests the function can be retrieved without errors.
     */
    @Test
    public void getFunction() {
        IFunction function = Function.getFunction(FAKE, match, target, parameters);
        Assert.assertNotNull("Expected to get function", function);
        function.configure();
        Assert.assertEquals("Wrong function resolution", FOO + BAR, function.resolve());
        Mockito.verify(match, Mockito.never()).error(Mockito.anyString());
        Mockito.verify(match, Mockito.never()).error(Mockito.<Exception>anyObject());
    }

}
