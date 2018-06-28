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
import match.IMatch;
import match.ITarget;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class Find extends Function {

    private IExpression mDirectory;
    private IExpression mPattern;
    private Set<String> mFiles = new HashSet<String>();

    public Find(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        if (hasParameter(DIRECTORY)) {
            mDirectory = getParameter(DIRECTORY);
            mPattern = getParameter(PATTERN);
        } else {
            mDirectory = getParameter(ANONYMOUS);
        }
        target.setName("Find:" + mPattern + " " + mDirectory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        File match = mTarget.getFile();
        File matchDir = match.getParentFile();
        Path matchDirPath = matchDir.toPath();
        String dir = mDirectory.resolve();
        File directory = null;
        if (dir == null || dir.isEmpty()) {
            directory = matchDir;
        } else {
            directory = new File(matchDir, dir);
        }
        Path dirPath = directory.toPath();
        String path = matchDirPath.relativize(dirPath).toString();
        if (!path.isEmpty()) {
            path += "/";
        }
        // System.out.println("Searching path " + path);
        scanFiles(directory, path, mFiles, mPattern == null ? ".*" : mPattern.resolve());
        // System.out.println("Found " + mFiles);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> resolveList() {
        List<String> files = new ArrayList<String>();
        for (String file : mFiles) {
            File f = new File(mTarget.getFile().getParentFile(), file);
            String path = f.toPath().normalize().toAbsolutePath().toString();
            mMatch.awaitFile(path);
            files.add(file);
        }
        return files;
    }

    public static void scanFiles(File directory, String path, Collection<String> files, String pattern) {
        scanFiles(directory, path, files, Pattern.compile(pattern));
    }

    public static void scanFiles(File directory, String path, Collection<String> files, Pattern pattern) {
        for (File file : directory.listFiles()) {
            String filename = file.getName();
            String fullname = path + filename;
            if (file.isFile()) {
                if (pattern.matcher(filename).matches()
                        || pattern.matcher(fullname).matches()) {
                    files.add(fullname);
                }
            } else {
                scanFiles(file, fullname + "/", files, pattern);
            }
        }
    }
}
