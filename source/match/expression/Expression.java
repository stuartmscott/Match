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

package match.expression;

import java.util.Arrays;
import java.util.List;

import match.IMatch;
import match.ITarget;
import match.Utilities;

public abstract class Expression implements IExpression {

    protected IMatch match;
    protected ITarget target;

    public Expression(IMatch match, ITarget target) {
        this.match = match;
        this.target = target;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {}

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> resolveList() {
        return Arrays.asList(resolve());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return resolve();
    }

}
