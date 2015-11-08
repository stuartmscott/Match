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

import expression.Expression;
import expression.IExpression;
import expression.Literal;
import main.IMatch;
import main.ITarget;
import main.Match;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.Matchers;

public class GetFileTest {

    private static final String FOO = "foo";
    private static final String BAR = "bar";
    private static final String NAME = "name";
    private static final String VALUE = "value";

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
    public void get() {
        IMatch match = Mockito.mock(IMatch.class);
        ITarget target = Mockito.mock(ITarget.class);
        mFile = SetFileTest.setFile(match, target);
        mFilename = mFile.getAbsolutePath();
        Mockito.when(match.getProperty(FOO)).thenReturn(mFilename);
        IFunction function = getFunction(match, target);
        function.setUp();
        Assert.assertEquals("Wrong function resolution", mFilename, function.resolve());
        Mockito.verify(match, Mockito.times(1)).setProperty(FOO, mFilename);
        Mockito.verify(match, Mockito.times(1)).addFile(mFilename);
        Mockito.verify(match, Mockito.times(1)).provideFile(mFilename);
    }

    private IFunction getFunction(IMatch match, ITarget target) {
        Map<String, IExpression> parameters = new HashMap<String, IExpression>();
        parameters.put(NAME, new Literal(match, target, FOO));
        parameters.put(VALUE, new Literal(match, target, mFilename));
        return new GetFile(match, target, parameters);
    }
}
