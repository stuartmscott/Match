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

package match.expression.function;

import java.util.Map;

import match.IMatch;
import match.ITarget;
import match.Utilities;
import match.expression.IExpression;

/**
 * A function to pick an option based on the build platform.
 */
public class Platform extends Function {

    public static final String LINUX = "linux";
    public static final String MAC = "mac";
    public static final String WINDOWS = "windows";

    private final IExpression expression;

    /**
     * Initializes the function with the given parameters.
     */
    public Platform(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        target.setName("Platform");
        if (Utilities.isLinux()) {
            expression = getParameter(LINUX);
        } else if (Utilities.isMac()) {
            expression = getParameter(MAC);
        } else if (Utilities.isWindows()) {
            expression = getParameter(WINDOWS);
        } else {
            expression = null;
            match.error("Unrecognized platform");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        expression.configure();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        return expression.resolve();
    }
}
