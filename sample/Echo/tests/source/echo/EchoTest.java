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

import org.junit.Assert;
import org.junit.Test;

public class EchoTest {

    @Test
    public void testEmpty() {
        Echo e = new Echo();
        Assert.assertEquals("Empty Echo", "", e.in(new String[0]));
        Assert.assertEquals("Empty Echo", "", e.in(new String[] {""}));
    }

    @Test
    public void testOne() {
        Echo e = new Echo();
        String[] s = new String[] {"blah"};
        Assert.assertEquals("One Echo", "blah", e.in(s));
    }

    @Test
    public void testTwo() {
        Echo e = new Echo();
        String[] s = new String[] {"foo", "bar"};
        Assert.assertEquals("Two Echoes", "foo bar", e.in(s));
    }

    @Test
    public void testMultiple() {
        Echo e = new Echo();
        String[] s = new String[] {"foo", "bar", "foobar", "blah"};
        Assert.assertEquals("Multiple Echoes", "foo bar foobar blah", e.in(s));
    }

}
