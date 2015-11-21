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
package main;

import expression.function.IFunction;

public class Target implements ITarget {

    private IMatch mMatch;
    private String mFile;
    private IFunction mFunction;

    public Target(IMatch match, String file) {
        mMatch = match;
        mFile = file;
    }

    /**
     * {inheritDoc}
     */
    @Override
    public String getFile() {
        return mFile;
    }

    /**
     * {inheritDoc}
     */
    @Override
    public void setFunction(IFunction function) {
        mFunction = function;
    }

    /**
     * {inheritDoc}
     */
    @Override
    public void configure() {
        mFunction.configure();
    }

    /**
     * {inheritDoc}
     */
    @Override
    public void build() {
        mFunction.resolve();
        // TODO put this target's input and output files in the database
    }

}
