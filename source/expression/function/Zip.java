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

public class Zip extends Function {

    public final static String ZIP_COMMAND = "zip -r %s %s";

    private IExpression mSource;
    private String mName;
    private File mOutputFile;
    private String mOutput;

    public Zip(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        IExpression name = getParameter(NAME);
        if (!(name instanceof Literal)) {
            mMatch.error("Zip function expects a String name");
        }
        mName = name.resolve();
        target.setName(mName);
        mSource = getParameter(SOURCE);
        mOutputFile = new File(target.getDirectory(), ZIP_OUTPUT + mName + ".zip");
        mOutput = mOutputFile.toPath().normalize().toAbsolutePath().toString();
        // TODO ensure zip isn't re-created if the inputs haven't been modified
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        mMatch.addFile(mOutput);
        mMatch.setProperty(mName, mOutput);
        mSource.configure();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        // Collect sources
        Set<String> sources = new HashSet<String>();
        sources.addAll(mSource.resolveList());
        System.out.println("Zip Sources: " + sources);
        List<String> sortedList = new ArrayList<String>(sources);
        Collections.sort(sortedList);
        String sourcesString = Utilities.join(" ", sortedList);
        System.out.println("Zip Sources: " + sourcesString);

        // Create output directory
        mTarget.runCommand(String.format(MKDIR_COMMAND, ZIP_OUTPUT));
        // Create zip
        mTarget.runCommand(String.format(ZIP_COMMAND, mOutput, sourcesString));
        mMatch.provideFile(mOutputFile);
        return mOutput;
    }
}
