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

public class Token {

    public String mFile;
    public int mLine;
    public Category mCategory;
    public String mValue;

    public Token(String file, int line, Category category) {
        this(file, line, category, "");
    }

    public Token(String file, int line, Category category, String value) {
        mFile = file;
        mLine = line;
        mCategory = category;
        mValue = value;
    }

    public String toString() {
        return mCategory + " : " + mValue;
    }

}
