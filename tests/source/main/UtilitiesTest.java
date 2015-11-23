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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class UtilitiesTest {

    private static final String SEPARATOR = ":";

    @Test
    public void join_none() {
        List<String> list = new ArrayList<>();
        Assert.assertEquals("Incorrect join", "", Utilities.join(SEPARATOR, list));
    }

    @Test
    public void join_single() {
        List<String> list = new ArrayList<>();
        list.add("foo");
        Assert.assertEquals("Incorrect join", "foo", Utilities.join(SEPARATOR, list));
    }

    @Test
    public void join_multiple() {
        List<String> list = new ArrayList<>();
        list.add("foo");
        list.add("bar");
        list.add("blah");
        Assert.assertEquals("Incorrect join", "foo:bar:blah", Utilities.join(SEPARATOR, list));
    }

    @Test
    public void newList() {
        List<String> list = Utilities.newList("foo");
        Assert.assertEquals("Incorrect list length", 1, list.size());
        Assert.assertEquals("Incorrect list element", "foo", list.get(0));
    }

}
