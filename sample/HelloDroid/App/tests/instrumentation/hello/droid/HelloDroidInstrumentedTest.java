/*
 * Copyright 2018 Stuart Scott
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

package hello.droid;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class HelloDroidInstrumentedTest {

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        // Test package name
        Assert.assertEquals("hello.droid", appContext.getPackageName());

        // Test string resources
        Assert.assertEquals("Hello Droid", appContext.getString(R.string.activity_name));

        // Test library string resources
        Assert.assertEquals("HelloDroid", appContext.getString(hello.droid.common.R.string.app_name));
        Assert.assertEquals("Hello Droid", appContext.getString(hello.droid.common.R.string.hello_droid));
        Assert.assertEquals("Say Hello", appContext.getString(hello.droid.common.R.string.say_hello));
    }
}
