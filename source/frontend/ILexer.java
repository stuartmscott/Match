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

public interface ILexer {

    /**
     * Gets the file being parsed.
     */
    File getFile();

    /**
     * Gets the filename being parsed.
     */
    String getFilename();

    /**
     * Moves the Lexer to the next Token.
     */
    void move();

    /**
     * Checks that the current token matches the given category.
     */
    boolean currentIs(Category category);

    /**
     * Asserts the current token matches the given category.
     *
     * @return the Token's value.
     */
    String match(Category category);

    /**
     * Returns the current token.
     */
    Token getCurrent();

    /**
     * Returns the category of the current token.
     */
    Category getCurrentCategory();

    /**
     * Returns the value of the current token.
     */
    String getCurrentValue();
}
