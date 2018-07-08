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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class containg utility useful functions.
 */
public final class Utilities {

    private Utilities() {}

    /**
     * Joins the collection of values together with the given separator.
     */
    public static <T> String join(String separator, Collection<T> values) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (T value : values) {
            if (first) {
                first = false;
            } else {
                sb.append(separator);
            }
            sb.append(value.toString());
        }
        return sb.toString();
    }

    /**
     * Returns true if the operating system is Linux.
     */
    public static boolean isLinux() {
        return System.getProperty("os.name").equals("Linux");
    }

    /**
     * Returns true if the operating system is Mac.
     */
    public static boolean isMac() {
        return System.getProperty("os.name").equals("Mac OS X");
    }

    /**
     * Returns true if the operating system is Windows.
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    /**
     * Writes the given string to the file.
     */
    public static void writeStringToFile(String string, File file) throws Exception {
        writeStringToFile(string, file, false);
    }

    /**
     * Writes the given string, optionally appending, to the file.
     */
    public static void writeStringToFile(String string, File file, boolean append) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, append));
        writer.write(string);
        writer.close();
    }
}
