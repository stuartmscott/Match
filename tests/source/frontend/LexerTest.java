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

import main.IMatch;
import main.Match;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class LexerTest {

    private IMatch mMatch;

    @Before
    public void setUp() {
        mMatch = Mockito.mock(IMatch.class);
    }

    @Test
    public void lex_empty() {
        Lexer lexer = createLexer(mMatch, "");
        Assert.assertEquals("Expected EOF when parsing an empty file", Category.EOF, lexer.getCurrentCategory());
    }

    @Test
    public void lex_newLine() {
        Lexer lexer = createLexer(mMatch, "myIdentifier\n");
        Assert.assertEquals("Line numbering should start at 1", 1, lexer.mLineNumber);
        lexer.move();
        Assert.assertEquals("Expected line number to increase", 2, lexer.mLineNumber);
    }

    @Test
    public void lex_whitespace() {
        Lexer lexer = createLexer(mMatch, "          \t   myIdentifier");
        // Expect whitespace to be skipped
        Assert.assertEquals("Expected identifier", Category.IDENTIFIER, lexer.getCurrentCategory());
        lexer.move();
        Assert.assertEquals("Expected EOF", Category.EOF, lexer.getCurrentCategory());
    }

    @Test
    public void lex_comment() {
        Lexer lexer = createLexer(mMatch, "# Test Comment\n myIdentifier");
        // Expect comments to be skipped
        Assert.assertEquals("Expected identifier", Category.IDENTIFIER, lexer.getCurrentCategory());
        lexer.move();
        Assert.assertEquals("Expected EOF", Category.EOF, lexer.getCurrentCategory());
    }

    @Test
    public void lex_brackets() {
        Lexer lexer = createLexer(mMatch, "()[]");
        Assert.assertEquals("Expected ORB", Category.ORB, lexer.getCurrentCategory());
        lexer.move();
        Assert.assertEquals("Expected CRB", Category.CRB, lexer.getCurrentCategory());
        lexer.move();
        Assert.assertEquals("Expected OSB", Category.OSB, lexer.getCurrentCategory());
        lexer.move();
        Assert.assertEquals("Expected CSB", Category.CSB, lexer.getCurrentCategory());
        lexer.move();
        Assert.assertEquals("Expected EOF", Category.EOF, lexer.getCurrentCategory());
    }

    @Test
    public void lex_literals() {
        Lexer lexer = createLexer(mMatch, "\"Test Literal\"");
        Assert.assertEquals("Expected string literal", Category.STRING_LITERAL, lexer.getCurrentCategory());
        lexer.move();
        Assert.assertEquals("Expected EOF", Category.EOF, lexer.getCurrentCategory());
    }

    @Test
    public void lex_identifier() {
        Lexer lexer = createLexer(mMatch, "myIdentifier");
        Assert.assertEquals("Expected identifier", Category.IDENTIFIER, lexer.getCurrentCategory());
        lexer.move();
        Assert.assertEquals("Expected EOF", Category.EOF, lexer.getCurrentCategory());
    }

    @Test
    public void lex_match() {
        Lexer lexer = createLexer(mMatch, "myIdentifier");
        Assert.assertEquals("Expected identifier", "myIdentifier", lexer.match(Category.IDENTIFIER));
        Mockito.verify(mMatch, Mockito.never()).error(Mockito.anyString());
    }

    @Test
    public void lex_noMatch() {
        Lexer lexer = createLexer(mMatch, "myIdentifier");
        lexer.match(Category.COMMENT);
        Mockito.verify(mMatch).error(Mockito.eq("match:1 unexpected \"COMMENT\", found \"IDENTIFIER (myIdentifier)\""));
    }

    @Test
    public void lex() {
        String input = "function_fake(name = \"Blah\" foo = function_fake() values = [ function_fake(\"bar\") \"far\"])"; 
        Lexer lexer = LexerTest.createLexer(mMatch, input);
        Category[] categories = new Category[] {
            Category.IDENTIFIER,
            Category.ORB,
            Category.IDENTIFIER,
            Category.ASSIGN,
            Category.STRING_LITERAL,
            Category.IDENTIFIER,
            Category.ASSIGN,
            Category.IDENTIFIER,
            Category.ORB,
            Category.CRB,
            Category.IDENTIFIER,
            Category.ASSIGN,
            Category.OSB,
            Category.IDENTIFIER,
            Category.ORB,
            Category.STRING_LITERAL,
            Category.CRB,
            Category.STRING_LITERAL,
            Category.CSB,
            Category.CRB
        };
        String[] values = new String[] {
            "function_fake",
            "(",
            "name",
            "=",
            "\"Blah\"",
            "foo",
            "=",
            "function_fake",
            "(",
            ")",
            "values",
            "=",
            "[",
            "function_fake",
            "(",
            "\"bar\"",
            ")",
            "\"far\"",
            "]",
            ")",
        };
        for (int i = 0; i < 20; i++) {
            Assert.assertEquals("Wrong category for " + i, values[i], lexer.match(categories[i]));
        }
        Mockito.verify(mMatch, Mockito.never()).error(Mockito.anyString());
    }

    static Lexer createLexer(IMatch match, String input) {
        InputStream in = new ByteArrayInputStream(input.getBytes());
        Lexer lexer = new Lexer(match, Match.LEXEMS, Match.MATCH, in);
        lexer.move();
        return lexer;
    }
}
