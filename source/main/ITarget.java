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

import expression.function.IFunction;

public interface ITarget {

    /**
     * Sets the function that will build this target.
     */
    void setFunction(IFunction function);

    /**
     * Set up the environment to build this target.
     */
    void setUp();

    /**
     * Build this target.
     */
    void build();

    /**
     * Tear down anything that was temporary.
     */
    void tearDown();
}
