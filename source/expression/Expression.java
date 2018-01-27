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
package expression;

import main.IMatch;
import main.ITarget;
import main.Utilities;

import java.util.List;

public abstract class Expression implements IExpression {

    protected IMatch mMatch;
    protected ITarget mTarget;

    public Expression(IMatch match, ITarget target) {
        mMatch = match;
        mTarget = target;
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
        return Utilities.newList(resolve());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return resolve();
    }

}
