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
package frontend;

import expression.function.FunctionFake;
import expression.function.IFunction;
import expression.function.Get;
import expression.IExpression;
import main.IMatch;
import main.ITarget;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ParserTest {

    private IMatch mMatch;
    private ILexer mLexer;
    private IParser mParser;

    @Before
    public void setUp() {
        mMatch = Mockito.mock(IMatch.class);
        mLexer = Mockito.mock(ILexer.class);
        mParser = new Parser(mMatch, mLexer);
    }

    @Test
    public void parse() {
        //List<ITarget> targets = mParser.parse();
        //Assert.assertEquals("Incorrect number of targets", 2, targets.size());
    }

    @Test
    public void matchFunction() {
        Mockito.when(mLexer.match(Category.IDENTIFIER)).thenReturn("function_fake");
        Mockito.when(mLexer.match(Category.ORB)).thenReturn("(");
        Mockito.when(mLexer.currentIs(Category.CRB)).thenReturn(true);
        Mockito.when(mLexer.match(Category.CRB)).thenReturn(")");
        IFunction function = mParser.matchFunction();
        Assert.assertNotNull("Couldn't load function", function);
        Assert.assertEquals("Wrong class", FunctionFake.class, function.getClass());
        Mockito.verify(mLexer, Mockito.times(3)).match(Mockito.<Category>anyObject());
        Mockito.verify(mLexer, Mockito.times(1)).currentIs(Category.CRB);
        Mockito.verify(mMatch, Mockito.never()).error(Mockito.anyString());
        Mockito.verify(mMatch, Mockito.never()).error(Mockito.<Exception>anyObject());
    }

    @Test
    public void matchFunction_doesntExist() {
        Mockito.when(mLexer.match(Category.IDENTIFIER)).thenReturn("missing_fake");
        Mockito.when(mLexer.match(Category.ORB)).thenReturn("(");
        Mockito.when(mLexer.currentIs(Category.CRB)).thenReturn(true);
        Mockito.when(mLexer.match(Category.CRB)).thenReturn(")");
        IFunction function = mParser.matchFunction();
        Assert.assertNull("Function shouldn't exist", function);
        Mockito.verify(mMatch, Mockito.times(1)).error("couldn't load function \"missing_fake\"");
    }

    @Test
    public void matchParameters_empty() {
        Mockito.when(mLexer.match(Category.ORB)).thenReturn("(");
        Mockito.when(mLexer.currentIs(Category.CRB)).thenReturn(true);
        Mockito.when(mLexer.match(Category.CRB)).thenReturn(")");
        Map<String, IExpression> parameters = mParser.matchParameters();
        Assert.assertNotNull("Expected parameter map", parameters);
        Assert.assertEquals("Expected no parameters", 0, parameters.size());
    }

    @Test
    public void matchParameters_single() {
        Mockito.when(mLexer.match(Category.ORB)).thenReturn("(");
        Mockito.when(mLexer.currentIs(Category.CRB)).thenReturn(false).thenReturn(true).thenReturn(true);
        Mockito.when(mLexer.currentIs(Category.IDENTIFIER)).thenReturn(true);
        Mockito.when(mLexer.match(Category.IDENTIFIER)).thenReturn("name");
        Mockito.when(mLexer.match(Category.ASSIGN)).thenReturn("=");
        Mockito.when(mLexer.getCurrentCategory()).thenReturn(Category.STRING_LITERAL);
        Mockito.when(mLexer.match(Category.STRING_LITERAL)).thenReturn("\"Blah\"");
        Mockito.when(mLexer.match(Category.CRB)).thenReturn(")");
        Map<String, IExpression> parameters = mParser.matchParameters();
        Assert.assertNotNull("Expected parameter map", parameters);
        Assert.assertEquals("Expected one parameters", 1, parameters.size());
        IExpression parameter = parameters.get("name");
        Assert.assertNotNull("Expected parameter", parameter);
        Assert.assertEquals("Incorrect parameter", "Blah", parameter.resolve());
    }

    @Test
    public void matchParameters_multiple() {
        String input = "(name = \"Blah\" foo = function_fake() values = [ function_fake(\"bar\") \"far\"])"; 
        Lexer lexer = LexerTest.createLexer(mMatch, input);
        Parser parser = new Parser(mMatch, lexer);
        Map<String, IExpression> parameters = parser.matchParameters();
        Assert.assertNotNull("Expected parameter map", parameters);
        Assert.assertEquals("Expected one parameters", 3, parameters.size());
        IExpression parameter = parameters.get("name");
        Assert.assertNotNull("Expected parameter", parameter);
        Assert.assertEquals("Incorrect parameter", "Blah", parameter.resolve());
        parameter = parameters.get("foo");
        Assert.assertNotNull("Expected parameter", parameter);
        Assert.assertEquals("Incorrect parameter", "foobar", parameter.resolve());
        parameter = parameters.get("values");
        Assert.assertNotNull("Expected parameter", parameter);
        Assert.assertEquals("Incorrect values", "bar;far", parameter.resolve());
    }

    @Test
    public void matchExpression_function() {
    }

    @Test
    public void matchExpression_list() {
    }

    @Test
    public void matchExpression_literal() {
    }

}
