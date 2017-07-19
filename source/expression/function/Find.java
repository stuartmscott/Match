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

import expression.IExpression;
import expression.Literal;
import main.IMatch;
import main.ITarget;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Find extends Function {

    private IExpression mDirectory;
    private IExpression mPattern;
    private List<String> mFiles = new ArrayList<String>();

    public Find(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        if (hasParameter(DIRECTORY)) {
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
    public void configure() {
        int index = new File(".").getAbsolutePath().length() - 1;
        File root = mTarget.getFile().getParentFile();
        File directory = new File(root, mDirectory.resolve());
        String path = directory.getAbsolutePath().substring(index);
        String pattern = mPattern == null ? ".*" : mPattern.resolve();
        scanFiles(directory, path, mFiles, Pattern.compile(pattern));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> resolveList() {
        List<String> files = new ArrayList<String>();
        for (String file : mFiles) {
            mMatch.awaitFile(file);
            files.add(new Literal(mMatch, mTarget, file).resolve());
        }
        return files;
    }

    public static void scanFiles(File directory, String path, List<String> files, Pattern pattern) {
        for (File file : directory.listFiles()) {
            String fullname = String.format("%s/%s", path, file.getName());
            if (file.isFile()) {
                if (pattern.matcher(fullname).matches()) {
                    files.add(fullname);
                }
            } else {
                scanFiles(file, fullname, files, pattern);
            }
        }
    }
}
