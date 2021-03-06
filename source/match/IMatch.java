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

import java.io.File;

public interface IMatch {

    /**
     * Gets the top level directory.
     *
     * @return the directory.
     */
    File getRootDir();

    /**
     * Gets the directory holding the build's libraries.
     *
     * @return the library directory.
     */
    File getLibrariesDir();

    /**
     * Checks if this build is cleaning.
     *
     * @return true iff this execution is cleaning.
     */
    boolean isCleaning();

    /**
     * Checks if this build is quiet.
     *
     * @return true iff this execution is quiet.
     */
    boolean isQuiet();

    /**
     * Checks if this build is verbose.
     *
     * @return true iff this execution is verbose.
     */
    boolean isVerbose();

    /**
     * Checks if the property has been set.
     *
     * @return true if the property for the given key is defined in this build.
     */
    boolean hasProperty(String key);

    /**
     * Gets the property for the given key from this build.
     */
    String getProperty(String key);

    /**
     * Sets the property for this build.
     */
    void setProperty(String key, String value);

    /**
     * Prints the warning to the console.
     */
    void warn(String message);

    /**
     * Prints the message to the console.
     */
    void println(String message);

    /**
     * Aborts the build and prints the message to the console.
     */
    void error(String message);

    /**
     * Aborts the build and prints the exception to the console.
     */
    void error(Exception exception);

    /**
     * Adds the given file to the target's output.
     */
    void addFile(String file);

    /**
     * Adds the directory and it's children to the target's output.
     */
    void addDirectory(File directory);

    /**
     * Called when a file is ready to be used by other target.
     *
     * <p>
     * This will allow all targets that are awaiting this file to continue.
     * </p>
     */
    void provideFile(File file);

    /**
     * Waits until the given file has been created.
     */
    void awaitFile(String file);
}
