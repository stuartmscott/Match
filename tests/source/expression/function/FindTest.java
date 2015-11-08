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
import main.MatchTest;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class FindTest {

    private static final String FOO = "foo";
    private static final String BAR = ".*/bar";
    private static final Set<String> FILESA = new HashSet<String>();
    private static final Set<String> FILESB = new HashSet<String>();
    static {
        FILESA.add("./a/b");
        FILESA.add("./c/d/e");
        FILESA.add("./c/d/f");
        FILESA.add("./bar");
        FILESB.add("./bar");
    }

    private File mRoot;

    @Before
    public void setUp() throws IOException {
        mRoot = MatchTest.createFileStructure();
    }

    @After
    public void tearDown() throws IOException {
        MatchTest.deleteFileStructure(mRoot);
    }

    @Test
    public void resolveAnonymous() {
        resolve(FILESA, Function.ANONYMOUS, mRoot.getAbsolutePath());
    }

    @Test
    public void resolveNamed() {
        resolve(FILESB, Find.DIRECTORY, mRoot.getAbsolutePath(), Find.PATTERN, BAR);
    }

    private void resolve(Set<String> expected, String... values) {
        IMatch match = Mockito.mock(IMatch.class);
        ITarget target = Mockito.mock(ITarget.class);
        Map<String, IExpression> parameters = new HashMap<String, IExpression>();
        for (int i = 0; i < values.length; i++) {
            parameters.put(values[i], new Literal(match, target, values[++i]));
        }
        IFunction function = new Find(match, target, parameters);
        function.setUp();
        List<String> actual = function.resolveList();
        Assert.assertEquals("Wrong number of files", expected.size(), actual.size());
        for (String file : actual) {
            Assert.assertTrue(String.format("%s not found", file), expected.contains(file));
        }
    }

}
