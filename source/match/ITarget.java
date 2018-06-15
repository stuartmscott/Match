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
package match;

import expression.function.IFunction;

import java.io.File;

public interface ITarget {

    /**
     * @return the target name
     */
    String getName();

    /**
     * Sets the target name
     */
    void setName(String name);

    /**
     * @return the match file
     */
    File getFile();

    /**
     * @return the match file's enclosing directory
     */
    File getDirectory();

    /**
     * Sets the function that will build this target.
     */
    void setFunction(IFunction function);

    /**
     * Set up the environment to build this target.
     */
    void configure();

    /**
     * Build this target.
     */
    void build();

    /**
     * @return true if this target has been built
     */
    boolean isBuilt();

    /**
     * Runs the given command in the target's enclosing directory and returns the exit code.
     */
    int runCommand(String command);

    /**
     * @return the most recent command run by this target.
     */
    String getLastCommand();

}
