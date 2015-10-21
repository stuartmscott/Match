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

public class SetTest {

    private static final String FOO = "foo";
    private static final String BAR = "bar";

    @Test
    public void get() {
        IMatch match = Mockito.mock(IMatch.class);
        ITarget target = Mockito.mock(ITarget.class);
        Map<String, IExpression> parameters = new HashMap<String, IExpression>();
        Literal literal = new Literal(match, target, BAR);
        parameters.put(FOO, literal);
        IFunction function = new Set(match, target, parameters);
        Assert.assertEquals("Wrong function resolution", BAR, function.resolve());
        Mockito.verify(target, Mockito.times(1)).setProperty(FOO, BAR);
    }

}
