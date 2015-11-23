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

public interface IMatch {

    /**
     * Gets the property for the given key from this build.
     */
    String getProperty(String key);

    /**
     * Sets the property for this build.
     */
    void setProperty(String key, String value);

    /**
     * Prints the message to the console.
     */
    void print(String message);

    /**
     * Prints the warning to the console.
     */
    void warn(String message);

    /**
     * Aborts the build and prints the message to the console.
     */
    void error(String message);

    /**
     * Aborts the build and prints the exception to the console.
     */
    void error(Exception exception);

    /**
     * Adds the given file to the target's output file.
     */
    void addFile(String file);

    /**
     * Called when a file is ready to be used by other target.
     *
     * This will allow all targets that are awaiting this file to continue.
     */
    void provideFile(String file);

    /**
     * Waits until the given file has been created.
     */
    void awaitFile(String file);

    /**
     * Runs the given command.
     */
    void runCommand(String command);

    /**
     * Sets whether the build runs silently.
     */
    void setQuiet(boolean quiet);

    /**
     * Sets whether the build prints verbosely.
     */
    void setVerbose(boolean verbose);
}
