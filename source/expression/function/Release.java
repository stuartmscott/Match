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

package expression.function;

import expression.IExpression;
import expression.Literal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import match.IMatch;
import match.ITarget;
import match.Utilities;

public class Release extends Function {

    public static final String AWAIT = "await";
    public static final String CHANNEL = "channel";

    private IExpression await;
    private IExpression channel;
    private String source;

    /**
     * Initializes the function with the given parameters.
     */
    public Release(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        IExpression s = getParameter(SOURCE);
        if (!(s instanceof Literal)) {
            match.error("Release function expects a String source");
        }
        source = s.resolve();
        target.setName("Release:" + source);
        await = getParameter(AWAIT);
        channel = getParameter(CHANNEL);
        // TODO ensure release isn't re-created if the inputs haven't been modified
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        await.configure();
        channel.configure();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        // Await checks
        for (String await : await.resolveList()) {
            match.awaitFile(match.getProperty(await));
        }
        // Get the source file
        String path = match.getProperty(source);
        match.awaitFile(path);
        // Push out distribution channels
        for (String channel : channel.resolveList()) {
            if (target.runCommand(String.format(channel, path)) != 0) {
                match.error("Failed to release via channel: " + channel);
            }
        }
        return "";
    }
}
