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

package match.frontend;

import java.io.File;

import match.IMatch;
import match.Match;
import match.Utilities;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

/**
 * Tests for Lexer.
 */
public class LexerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private IMatch match;

    @Before
    public void setUp() {
        match = Mockito.mock(IMatch.class);
    }

    @Test
    public void lex_empty() throws Exception {
        File file = folder.newFile("match");
        Utilities.writeStringToFile("", file);
        Lexer lexer = createLexer(match, file);
        Assert.assertEquals("Expected EOF when parsing an empty file", Category.EOF, lexer.getCurrentCategory());
    }

    @Test
    public void lex_newLine() throws Exception {
        File file = folder.newFile("match");
        Utilities.writeStringToFile("myIdentifier\n", file);
        Lexer lexer = createLexer(match, file);
        Assert.assertEquals("Line numbering should start at 1", 1, lexer.lineNumber);
        lexer.move();
        Assert.assertEquals("Expected line number to increase", 2, lexer.lineNumber);
    }

    @Test
    public void lex_whitespace() throws Exception {
        File file = folder.newFile("match");
        Utilities.writeStringToFile("          \t   myIdentifier", file);
        Lexer lexer = createLexer(match, file);
        // Expect whitespace to be skipped
        Assert.assertEquals("Expected identifier", Category.LOWER_CASE, lexer.getCurrentCategory());
        lexer.move();
        Assert.assertEquals("Expected EOF", Category.EOF, lexer.getCurrentCategory());
    }

    @Test
    public void lex_comment() throws Exception {
        File file = folder.newFile("match");
        Utilities.writeStringToFile("# Test Comment\n myIdentifier", file);
        Lexer lexer = createLexer(match, file);
        // Expect comments to be skipped
        Assert.assertEquals("Expected identifier", Category.LOWER_CASE, lexer.getCurrentCategory());
        lexer.move();
        Assert.assertEquals("Expected EOF", Category.EOF, lexer.getCurrentCategory());
    }

    @Test
    public void lex_brackets() throws Exception {
        File file = folder.newFile("match");
        Utilities.writeStringToFile("()[]", file);
        Lexer lexer = createLexer(match, file);
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
    public void lex_literals() throws Exception {
        File file = folder.newFile("match");
        Utilities.writeStringToFile("\"Test Literal\"", file);
        Lexer lexer = createLexer(match, file);
        Assert.assertEquals("Expected string literal", Category.STRING_LITERAL, lexer.getCurrentCategory());
        lexer.move();
        Assert.assertEquals("Expected EOF", Category.EOF, lexer.getCurrentCategory());
    }

    @Test
    public void lex_identifier() throws Exception {
        File file = folder.newFile("match");
        Utilities.writeStringToFile("myIdentifier", file);
        Lexer lexer = createLexer(match, file);
        Assert.assertEquals("Expected identifier", Category.LOWER_CASE, lexer.getCurrentCategory());
        lexer.move();
        Assert.assertEquals("Expected EOF", Category.EOF, lexer.getCurrentCategory());
    }

    @Test
    public void lex_match() throws Exception {
        File file = folder.newFile("match");
        Utilities.writeStringToFile("myIdentifier", file);
        Lexer lexer = createLexer(match, file);
        Assert.assertEquals("Expected identifier", "myIdentifier", lexer.match(Category.LOWER_CASE));
        Mockito.verify(match, Mockito.never()).error(Mockito.anyString());
    }

    @Test
    public void lex_noMatch() throws Exception {
        File file = folder.newFile("match");
        Utilities.writeStringToFile("myIdentifier", file);
        Lexer lexer = createLexer(match, file);
        lexer.match(Category.COMMENT);
        String filename = lexer.getFilename();
        Mockito.verify(match).error(Mockito.eq(String.format("%s:1 unexpected \"COMMENT\", found \"LOWER_CASE (myIdentifier)\"", filename)));
    }

    @Test
    public void lex() throws Exception {
        String input = "FunctionFake(name = \"Blah\" foo = FunctionFake() values = [ FunctionFake(\"bar\") \"far\"])"; 
        File file = folder.newFile("match");
        Utilities.writeStringToFile(input, file);
        Lexer lexer = createLexer(match, file);
        Category[] categories = new Category[] {
            Category.UPPER_CASE,
            Category.ORB,
            Category.LOWER_CASE,
            Category.ASSIGN,
            Category.STRING_LITERAL,
            Category.LOWER_CASE,
            Category.ASSIGN,
            Category.UPPER_CASE,
            Category.ORB,
            Category.CRB,
            Category.LOWER_CASE,
            Category.ASSIGN,
            Category.OSB,
            Category.UPPER_CASE,
            Category.ORB,
            Category.STRING_LITERAL,
            Category.CRB,
            Category.STRING_LITERAL,
            Category.CSB,
            Category.CRB
        };
        String[] values = new String[] {
            "FunctionFake",
            "(",
            "name",
            "=",
            "\"Blah\"",
            "foo",
            "=",
            "FunctionFake",
            "(",
            ")",
            "values",
            "=",
            "[",
            "FunctionFake",
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
        Mockito.verify(match, Mockito.never()).error(Mockito.anyString());
    }

    static Lexer createLexer(IMatch match, File file) throws Exception {
        Lexer lexer = new Lexer(match, Match.LEXEMS, file);
        lexer.move();
        return lexer;
    }
}
