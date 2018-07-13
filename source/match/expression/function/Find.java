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

package match.expression.function;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import match.IMatch;
import match.ITarget;
import match.expression.IExpression;
import match.expression.Literal;

/**
 * Finds all files in the given directory, optionally matching the given pattern.
 */
public class Find extends Function {

    private IExpression directory;
    private IExpression pattern;
    private Set<String> files = new HashSet<String>();

    /**
     * Initializes the function with the given parameters.
     */
    public Find(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        if (hasParameter(DIRECTORY)) {
            directory = getParameter(DIRECTORY);
            pattern = getParameter(PATTERN);
        } else {
            directory = getParameter(ANONYMOUS);
        }
        target.setName("Find:" + pattern + " " + directory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        File match = target.getFile();
        File matchDir = match.getParentFile();
        Path matchDirPath = matchDir.toPath();
        String dir = directory.resolve();
        File dirFile = null;
        if (dir == null || dir.isEmpty()) {
            dirFile = matchDir;
        } else {
            dirFile = new File(matchDir, dir);
        }
        Path dirPath = dirFile.toPath();
        String path = matchDirPath.relativize(dirPath).toString();
        if (!path.isEmpty()) {
            path += "/";
        }
        // System.out.println("Searching path " + path);
        scanFiles(dirFile, path, files, pattern == null ? ".*" : pattern.resolve());
        // System.out.println("Found " + files);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> resolveList() {
        List<String> fs = new ArrayList<String>();
        for (String file : files) {
            File f = new File(target.getFile().getParentFile(), file);
            String path = f.toPath().normalize().toAbsolutePath().toString();
            match.awaitFile(path);
            fs.add(file);
        }
        return fs;
    }

    /**
     * Scans the given directory adding all files matching the given pattern to the given collection.
     */
    public static void scanFiles(File directory, String path, Collection<String> files, String pattern) {
        scanFiles(directory, path, files, Pattern.compile(pattern));
    }

    /**
     * Scans the given directory adding all files matching the given pattern to the given collection.
     */
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
