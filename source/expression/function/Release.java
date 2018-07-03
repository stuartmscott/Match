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
import match.IMatch;
import match.ITarget;
import match.Utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class Release extends Function {

    public final static String AWAIT = "await";
    public final static String CHANNEL = "channel";

    private IExpression mAwait;
    private IExpression mChannel;
    private String mSource;

    public Release(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        IExpression source = getParameter(SOURCE);
        if (!(source instanceof Literal)) {
            mMatch.error("Release function expects a String source");
        }
        mSource = source.resolve();
        target.setName("Release:" + mSource);
        mAwait = getParameter(AWAIT);
        mChannel = getParameter(CHANNEL);
        // TODO ensure release isn't re-created if the inputs haven't been modified
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        mAwait.configure();
        mChannel.configure();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        // Await checks
        for (String await : mAwait.resolveList()) {
            mMatch.awaitFile(mMatch.getProperty(await));
        }
        // Get the source file
        String source = mMatch.getProperty(mSource);
        mMatch.awaitFile(source);
        // Push out distribution channels
        for (String channel : mChannel.resolveList()) {
            if (mTarget.runCommand(String.format(channel, source)) != 0) {
                mMatch.error("Failed to release via channel: " + channel);
            }
        }
        return "";
    }
}
