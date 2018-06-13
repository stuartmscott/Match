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

import config.Config;
import expression.IExpression;
import expression.Literal;
import match.IMatch;
import match.ITarget;
import match.MatchTest;

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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

public class FindTest {

    private static final String C = "c";
    private static final String C_D_E = "c/d/e";
    private static final String BAR = ".*bar";
    private final Set<String> filesA = new HashSet<String>();
    private final Set<String> filesB = new HashSet<String>();
    private final Set<String> filesC = new HashSet<String>();
    private final Set<String> filesD = new HashSet<String>();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    public File root;
    public String rootPath;
    public Config config;

    @Before
    public void setUp() throws IOException {
        root = folder.getRoot();
        MatchTest.createFileStructure(root);
        config = new Config();
        config.put("root", root.toPath().toString());
        filesA.add("a/b");
        filesA.add("c/d/e");
        filesA.add("c/d/f");
        filesA.add("bar");
        filesB.add("bar");
        filesC.add("c/d/e");
        filesC.add("c/d/f");
        filesD.add("c/d/e");
    }

    @After
    public void tearDown() throws IOException {
        //
    }

    @Test
    public void resolveAnonymous() {
        resolve(filesA, Function.ANONYMOUS, "");
    }

    @Test
    public void resolvePattern() {
        resolve(filesB, Find.DIRECTORY, "", Find.PATTERN, BAR);
    }

    @Test
    public void resolveNamed() {
        resolve(filesC, Find.ANONYMOUS, C);
    }

    @Test
    public void resolveNamedPattern() {
        resolve(filesD, Find.DIRECTORY, C, Find.PATTERN, C_D_E);
    }

    private void resolve(Set<String> expected, String... values) {
        IMatch match = Mockito.mock(IMatch.class);
        ITarget target = Mockito.mock(ITarget.class);
        Mockito.when(match.getRootDir()).thenReturn(root);
        Mockito.when(target.getFile()).thenReturn(new File(root, "match"));
        Map<String, IExpression> parameters = new HashMap<String, IExpression>();
        for (int i = 0; i < values.length; i++) {
            parameters.put(values[i], new Literal(match, target, values[++i]));
        }
        IFunction function = new Find(match, target, parameters);
        function.configure();
        List<String> actual = function.resolveList();
        Assert.assertEquals("Wrong number of files", expected.size(), actual.size());
        Assert.assertEquals("Wrong files", expected.toString(), actual.toString());
        for (String file : actual) {
            Assert.assertTrue(String.format("%s not found", file), expected.contains(file));
        }
    }

}
