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
import org.mockito.Matchers;

public class GetFileTest {

    private static final String FOO = "foo";
    private static final String BAR = "bar";
    private static final String NAME = "name";
    private static final String VALUE = "value";

    @Test
    public void get() {
        Match match = new Match(null);
        ITarget target = Mockito.mock(ITarget.class);
        SetFileTest.setFile(match, target);
        IFunction function = getFunction(match, target);
        function.setUp();
        Mockito.verify(match, Mockito.times(1)).setProperty(FOO, BAR);
        Mockito.verify(match, Mockito.times(1)).addFile(BAR);
        Mockito.verify(match, Mockito.times(1)).provideFile(BAR);
        Assert.assertEquals("Wrong function resolution", BAR, function.resolve());
    }

    private IFunction getFunction(IMatch match, ITarget target) {
        Map<String, IExpression> parameters = new HashMap<String, IExpression>();
        parameters.put(NAME, new Literal(match, target, FOO));
        parameters.put(VALUE, new Literal(match, target, BAR));
        return new SetFile(match, target, parameters);
    }
}
