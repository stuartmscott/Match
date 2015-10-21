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

public class FunctionTest {

    private static final String FOO = "foo";
    private static final String BAR = "bar";
    private static final String BLAH = "blah";

    @Test
    public void parameters() {
        IMatch match = Mockito.mock(IMatch.class);
        ITarget target = Mockito.mock(ITarget.class);
        Map<String, IExpression> parameters = new HashMap<String, IExpression>();
        Literal literal = new Literal(match, target, BAR);
        parameters.put(FOO, literal);
        IFunction function = new FunctionImpl(match, target, parameters);
        Assert.assertTrue("Expected foo", function.hasParameter(FOO));
        Assert.assertFalse("Unexpected blah", function.hasParameter(BLAH));
        Assert.assertEquals("Wrong parameter", literal, function.getParameter(FOO));
    }

    @Test
    public void getFunction() throws Exception {
        IMatch match = Mockito.mock(IMatch.class);
        ITarget target = Mockito.mock(ITarget.class);
        Map<String, IExpression> parameters = new HashMap<String, IExpression>();
        IFunction function = Function.getFunction(BLAH, match, target, parameters);
        Assert.assertNotNull("Expected to get function", function);
        Assert.assertEquals("Wrong function resolution", FOO, function.resolve());
    }

    private static class FunctionImpl extends Function {

        public FunctionImpl(IMatch match, ITarget target, Map<String, IExpression> parameters) {
            super(match, target, parameters);
        }

        public String resolve() {
            return FOO;
        }
    }

    static {
        Function.register(FunctionImpl.class, BLAH);
    }
}
