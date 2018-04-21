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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class Utilities {

    private Utilities() {}

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

    public static <T> List<T> newList(T element) {
        List<T> list = new ArrayList<T>();
        list.add(element);
        return list;
    }

    public static boolean isAndroid() {
        return false;
    }

    public static boolean isLinux() {
        return false;
    }

    public static boolean isMac() {
        return System.getProperty("os.name").equals("Mac OS X");
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }
}
