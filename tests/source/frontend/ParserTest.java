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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import main.IMatch;
import main.ITarget;
import main.Match;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import expression.IExpression;
import expression.function.FunctionFake;
import expression.function.IFunction;

public class ParserTest {

    private IMatch mMatch;

    @Before
    public void setUp() {
        mMatch = Mockito.mock(IMatch.class);
    }

    @Test
    public void parse() {
        String input = "function_fake(name = \"Target1\") function_fake(name = \"Target2\")";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        Lexer lexer = new Lexer(mMatch, Match.LEXEMS, Match.MATCH, in);
        Parser parser = new Parser(mMatch, lexer);
        List<ITarget> targets = parser.parse();
        Assert.assertEquals("Incorrect number of targets", 2, targets.size());
    }

    @Test
    public void matchFunction() {
        String input = "function_fake()";
        Lexer lexer = LexerTest.createLexer(mMatch, input);
        Parser parser = new Parser(mMatch, lexer);
        IFunction function = parser.matchFunction();
        Assert.assertNotNull("Couldn't load function", function);
        Assert.assertEquals("Wrong class", FunctionFake.class, function.getClass());
        Mockito.verify(mMatch, Mockito.never()).error(Mockito.anyString());
        Mockito.verify(mMatch, Mockito.never()).error(Mockito.<Exception>anyObject());
    }

    @Test
    public void matchFunction_doesntExist() {
        String input = "missing_fake()";
        Lexer lexer = LexerTest.createLexer(mMatch, input);
        Parser parser = new Parser(mMatch, lexer);
        IFunction function = parser.matchFunction();
        Assert.assertNull("Function shouldn't exist", function);
        Mockito.verify(mMatch, Mockito.times(1)).error(Mockito.<Exception>anyObject());
    }

    @Test
    public void matchParameters_empty() {
        String input = "()";
        Lexer lexer = LexerTest.createLexer(mMatch, input);
        Parser parser = new Parser(mMatch, lexer);
        Map<String, IExpression> parameters = parser.matchParameters();
        Assert.assertNotNull("Expected parameter map", parameters);
        Assert.assertEquals("Expected no parameters", 0, parameters.size());
    }

    @Test
    public void matchParameters_single() {
        String input = "(name = \"Blah\")";
        Lexer lexer = LexerTest.createLexer(mMatch, input);
        Parser parser = new Parser(mMatch, lexer);
        Map<String, IExpression> parameters = parser.matchParameters();
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
        Assert.assertEquals("Incorrect values", "bar far", parameter.resolve());
    }

    @Test
    public void matchExpression_list() {
        String input = "[\"Blah\" function_fake()]"; 
        Lexer lexer = LexerTest.createLexer(mMatch, input);
        Parser parser = new Parser(mMatch, lexer);
        IExpression expression = parser.matchExpression();
        List<String> list = expression.resolveList();
        Assert.assertEquals("Incorrect list size", 2, list.size());
        Assert.assertEquals("Incorrect list element", "Blah", list.get(0));
        Assert.assertEquals("Incorrect list element", "foobar", list.get(1));
    }

    @Test
    public void matchExpression_literal() {
        String input = "\"Blah\""; 
        Lexer lexer = LexerTest.createLexer(mMatch, input);
        Parser parser = new Parser(mMatch, lexer);
        IExpression expression = parser.matchExpression();
        Assert.assertEquals("Incorrect list element", "Blah", expression.resolve());
    }

}
