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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import match.IMatch;

public class Lexer implements ILexer {

    public IMatch match;
    public File file;
    public String filename;
    public int lineNumber = 1;
    public List<Lexem> lexems;
    public InputStream input;

    public int inputInt;
    public String inputChar;
    public String currentValue = "";
    public String nextValue;
    public Token currentToken;

    /**
     * Creates a Lexer for the given file with the given lexems.
     */
    public Lexer(IMatch match, List<Lexem> lexems, File file) {
        this.match = match;
        this.lexems = lexems;
        this.file = file;
        filename = file.getAbsolutePath();
        try {
            input = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            match.error(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getFile() {
        return file;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFilename() {
        return filename;
    }

    private void error(String message) {
        match.error(String.format("%s:%d %s", filename, lineNumber, message));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void move() {
        currentToken = nextToken();
        if (currentToken == null) {
            error("end of file reached unexpectedly");
        }
    }

    private Token nextToken() {
        Token token = null;
        try {
            token = getNextToken();
        } catch (IOException e) {
            error("error reading file");
        }
        if (token != null) {
            if (token.category == Category.COMMENT || token.category == Category.NEWLINE) {
                lineNumber++;
                token = nextToken();
            } else if (token.category == Category.WHITESPACE) {
                token = nextToken();
            }
        }
        return token;
    }

    private Token getNextToken() throws IOException {
        while ((inputInt = input.read()) != -1) {
            inputChar = (char) inputInt + "";
            nextValue = currentValue + inputChar;
            Token currentToken = getToken(currentValue);
            Token nextToken = getToken(nextValue);

            if (currentToken == null) {
                currentValue = nextValue;
            } else {
                if (nextToken == null) {
                    currentValue = inputChar;
                    return currentToken;
                } else {
                    currentValue = nextValue;
                }
            }
        }
        if (!currentValue.isEmpty()) {
            Token leftover = getToken(currentValue);
            if (leftover == null) {
                error(String.format("couldn't parse %s", currentValue));
            } else {
                currentValue = "";
                return leftover;
            }
        }
        return new Token(lineNumber, Category.EOF);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean currentIs(Category category) {
        return currentToken.category == category;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String match(Category category) {
        if (!currentIs(category)) {
            error("unexpected \"" + category + "\", found \"" + currentToken.category + " (" + currentToken.value + ")\"");
        }
        String value = currentToken.value;
        if (!currentIs(Category.EOF)) {
            move();
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Token getCurrent() {
        return currentToken;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Category getCurrentCategory() {
        return currentToken.category;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCurrentValue() {
        return currentToken.value;
    }

    private Token getToken(String value) {
        for (Lexem lexem : lexems) {
            if (value.matches(lexem.regex)) {
                return new Token(lineNumber, lexem.category, value);
            }
        }
        return null;
    }
}
