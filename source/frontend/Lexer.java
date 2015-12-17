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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;

public class Lexer implements ILexer {

    public IMatch mMatch;
    public File mFile;
    public String mFilename;
    public int mLineNumber = 1;
    public List<Lexem> mLexems;
    public InputStream mInput;

    public int mInputInt;
    public String mInputChar;
    public String mCurrentValue = "";
    public String mNextValue;
    public Token mCurrentToken;

    public Lexer(IMatch match, List<Lexem> lexems, File file) {
        mMatch = match;
        mLexems = lexems;
        mFile = file;
        mFilename = file.getAbsolutePath();
        try {
            mInput = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            mMatch.error(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getFile() {
        return mFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFilename() {
        return mFilename;
    }

    private void error(String message) {
        mMatch.error(String.format("%s:%d %s", mFilename, mLineNumber, message));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void move() {
        mCurrentToken = nextToken();
        if (mCurrentToken == null) {
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
            if (token.mCategory == Category.COMMENT || token.mCategory == Category.NEWLINE) {
                mLineNumber++;
                token = nextToken();
            } else if (token.mCategory == Category.WHITESPACE) {
                token = nextToken();
            }
        }
        return token;
    }

    private Token getNextToken() throws IOException {
        while ((mInputInt = mInput.read()) != -1) {
            mInputChar = (char) mInputInt + "";
            mNextValue = mCurrentValue + mInputChar;
            Token currentToken = getToken(mCurrentValue);
            Token nextToken = getToken(mNextValue);

            if (currentToken == null) {
                mCurrentValue = mNextValue;
            } else {
                if (nextToken == null) {
                    mCurrentValue = mInputChar;
                    return currentToken;
                } else {
                    mCurrentValue = mNextValue;
                }
            }
        }
        if (!mCurrentValue.isEmpty()) {
            Token leftover = getToken(mCurrentValue);
            if (leftover == null) {
                error(String.format("couldn't parse %s", mCurrentValue));
            } else {
                mCurrentValue = "";
                return leftover;
            }
        }
        return new Token(mLineNumber, Category.EOF);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean currentIs(Category category) {
        return mCurrentToken.mCategory == category;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String match(Category category) {
        if (!currentIs(category)) {
            error("unexpected \"" + category + "\", found \"" + mCurrentToken.mCategory + " (" + mCurrentToken.mValue + ")\"");
        }
        String value = mCurrentToken.mValue;
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
        return mCurrentToken;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Category getCurrentCategory() {
        return mCurrentToken.mCategory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCurrentValue() {
        return mCurrentToken.mValue;
    }

    private Token getToken(String value) {
        for (Lexem lexem : mLexems) {
            if (value.matches(lexem.mRegex)) {
                return new Token(mLineNumber, lexem.mCategory, value);
            }
        }
        return null;
    }
}
