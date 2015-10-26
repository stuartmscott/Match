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
package expression;

import expression.function.IFunction;
import main.IMatch;
import main.ITarget;
import main.Match;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class ExpressionListTest {

    private static final String FOO = "foo";
    private static final String BAR = "bar";
    private static final String EXPECTED = "foo;bar";

    @Test
    public void list() {
        IMatch match = Mockito.mock(IMatch.class);
        ITarget target = Mockito.mock(ITarget.class);
        List<IExpression> elements = new ArrayList<IExpression>();
        elements.add(new Literal(match, target, FOO));
        elements.add(new Literal(match, target, BAR));
        IExpression expression = new ExpressionList(match, target, elements);
        Assert.assertEquals("Wrong resolution", EXPECTED, expression.resolve());
    }

}