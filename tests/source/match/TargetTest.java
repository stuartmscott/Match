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

import java.io.File;

import match.IMatch;
import match.ITarget;
import match.expression.function.IFunction;

import org.junit.Test;
import org.mockito.Mockito;

public class TargetTest {

    @Test
    public void build() {
        IMatch match = Mockito.mock(IMatch.class);
        IFunction function = Mockito.mock(IFunction.class);
        ITarget target = new Target(match, new File("/tmp/match"));
        target.setFunction(function);
        target.build();
        Mockito.verify(function, Mockito.times(1)).resolve();
    }

}
