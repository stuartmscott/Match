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
package expression.function;

import expression.IExpression;
import expression.Literal;
import match.IMatch;
import match.ITarget;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

public class SetFileTest {

    private static final String FOO = "foo";
    private static final String BAR = "bar";
    private static final String NAME = "name";
    private static final String VALUE = "value";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File mFile = null;
    private String mFilename;

    @After
    public void tearDown() {
        if (mFile != null) {
            mFile.delete();
            mFile = null;
        }
    }

    @Test
    public void setFile() throws Exception {
        IMatch match = Mockito.mock(IMatch.class);
        ITarget target = Mockito.mock(ITarget.class);
        mFile = folder.newFile(FOO);
        mFilename = mFile.toPath().toString();
        SetFileTest.setFile(match, target, mFile);
        Mockito.verify(match, Mockito.times(1)).setProperty(FOO, mFilename);
        Mockito.verify(match, Mockito.times(1)).addFile(mFilename);
        Mockito.verify(match, Mockito.times(1)).provideFile(mFile);
    }

    static void setFile(IMatch match, ITarget target, File file) throws Exception {
        Map<String, IExpression> parameters = new HashMap<String, IExpression>();
        parameters.put(NAME, new Literal(match, target, FOO));
        parameters.put(VALUE, new Literal(match, target, file.toPath().toString()));
        IFunction function = new SetFile(match, target, parameters);
        function.configure();
        function.resolve();
    }
}
