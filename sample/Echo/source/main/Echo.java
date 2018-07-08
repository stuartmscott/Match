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

package echo;

public class Echo {

    /**
     * Echoes the given arguments.
     *
     * @return a new string containing the echoed arguments.
     */
    public String in(String[] args) {
        StringBuilder sb = new StringBuilder();
        if (args.length > 0) {
            boolean isFirst = true;
            for (String s : args) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    sb.append(" ");
                }
                sb.append(s);
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        Echo e = new Echo();
        System.out.println(e.in(args));
    }

}
