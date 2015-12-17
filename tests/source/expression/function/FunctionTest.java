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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class FunctionTest {

    private static final String FOO = "foo";
    private static final String BAR = "bar";
    private static final String FAKE = "FunctionFake";

    private IMatch mMatch;
    private ITarget mTarget;
    private Map<String, IExpression> mParameters;

    @Before
    public void setUp() {
        mMatch = Mockito.mock(IMatch.class);
        mTarget = Mockito.mock(ITarget.class);
        mParameters = new HashMap<String, IExpression>();
    }

    @Test
    public void parameters() {
        Literal literal = new Literal(mMatch, mTarget, BAR);
        mParameters.put(FOO, literal);
        IFunction function = new FunctionFake(mMatch, mTarget, mParameters);
        Assert.assertEquals("Wrong parameter", BAR, function.getParameter(FOO).resolve());
    }

    @Test
    public void getFunction() {
        IFunction function = Function.getFunction(FAKE, mMatch, mTarget, mParameters);
        function.configure();
        Assert.assertNotNull("Expected to get function", function);
        function.configure();
        Assert.assertEquals("Wrong function resolution", FOO + BAR, function.resolve());
        Mockito.verify(mMatch, Mockito.never()).error(Mockito.anyString());
        Mockito.verify(mMatch, Mockito.never()).error(Mockito.<Exception>anyObject());
    }

}
