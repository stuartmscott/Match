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

import expression.ExpressionList;
import expression.IExpression;
import expression.Literal;
import main.IMatch;
import main.ITarget;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetFiles extends Function {

    static final String DIRECTORY = "directory";
    static final String PATTERN = "pattern";

    private IExpression mDirectory;
    private IExpression mPattern;
    private List<String> mFiles = new ArrayList<String>();

    public GetFiles(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        if (parameters.containsKey(DIRECTORY)) {
            mDirectory = getParameter(DIRECTORY);
            mPattern = getParameter(PATTERN);
        } else {
            mDirectory = getParameter(ANONYMOUS);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() {
        File directory = new File(mDirectory.resolve());
        String pattern = mPattern == null ? ".*" : mPattern.resolve();
        scanFiles(directory.getAbsolutePath().length(), directory, mFiles, Pattern.compile(pattern));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        List<IExpression> list = new ArrayList<IExpression>();
        for (String file : mFiles) {
            mMatch.awaitFile(file);
            list.add(new Literal(mMatch, mTarget, file));
        }
        return new ExpressionList(mMatch, mTarget, list).resolve();
    }

    private static void scanFiles(int pathLength, File directory, List<String> files, Pattern pattern) {
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                String fullname = String.format(".%s", file.getAbsolutePath().substring(pathLength));
                if (pattern.matcher(fullname).matches()) {
                    files.add(fullname);
                }
            } else {
                scanFiles(pathLength, file, files, pattern);
            }
        }
    }
}
