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

import org.junit.runners.Suite;
import org.junit.runner.RunWith;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    expression.function.FindTest.class,
    expression.function.FunctionTest.class,
    expression.function.GetFileTest.class,
    expression.function.GetTest.class,
    expression.function.JavaJarTest.class,
    expression.function.JavaJUnitTest.class,
    expression.function.SetFileTest.class,
    expression.function.SetTest.class,
    expression.function.ZipTest.class,
    expression.ExpressionListTest.class,
    expression.LiteralTest.class,
    frontend.LexerTest.class,
    frontend.ParserTest.class,
    match.MatchTest.class,
    match.TargetTest.class,
    match.UtilitiesTest.class
})
public class AllTests {
    //nothing
}
