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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class Match implements IMatch {

    private static final String MATCH = "match";

    private File mRoot;

    private Map<String, String> mProperties = new ConcurrentHashMap<String, String>();
    private Map<String, CountDownLatch> mFiles = new ConcurrentHashMap<String, CountDownLatch>();
    private final List<File> mMatchFiles = new ArrayList<File>();
    private final List<File> mAllFiles = new ArrayList<File>();

    public Match(File root) {
        mRoot = root;
    }

    List<File> getAllFiles() {
        return mAllFiles;
    }

    /**
     * {inheritDoc}
     */
    @Override
    public String getProperty(String key) {
        String value = mProperties.get(key);
        if (value == null) {
            error("%s was not set", key);
        }
        return value;
    }

    /**
     * {inheritDoc}
     */
    @Override
    public void setProperty(String key, String value) {
        mProperties.put(key, value);
    }

    /**
     * {inheritDoc}
     */
    @Override
    public void addFile(String file) {
        mFiles.put(file, new CountDownLatch(1));
    }

    /**
     * {inheritDoc}
     */
    @Override
    public void provideFile(String file) {
        CountDownLatch latch = mFiles.get(file);
        if (latch == null) {
            error("provideFile called before addFile for %s", file);
        }
        latch.countDown();
    }

    /**
     * {inheritDoc}
     */
    @Override
    public void awaitFile(String file) {
        CountDownLatch latch = mFiles.get(file);
        if (latch == null) {
            error("no targets provided %s", file);
        }
        try {
            latch.await();
        } catch(InterruptedException e) {
            error("target interrupted");
        }
    }

    private void loadFiles(File directory) {
        for (File child : directory.listFiles()) {
            if (child.isDirectory()) {
                loadFiles(child);
            } else {
                mAllFiles.add(child);
                if (child.getName().equals(MATCH)) {
                    mMatchFiles.add(child);
                }
            }
        }
    }

    void light() {
        loadFiles(mRoot);
        List<ITarget> targets = new ArrayList<ITarget>();
        for (ITarget target : targets) {
            target.setUp();
        }
        for (ITarget target : targets) {
            target.build();
        }
        for (ITarget target : targets) {
            target.tearDown();
        }
        // Create a thread for each target, but only start a thread if the number of targets that
        // aren't blocked is under MAX_THREADS. If all targets are blocked there is a deadlock.
        // Look at the output files of a target and all the files under the output directory,
        // delete files that were created in the last build but is no longer made by any targets.
        // This means all targets have to know their output files even if they dont need to build.
        // This is difficult for java compiles because you cannot know beforehand, given source
        // files, which classes will get generated because of inner/anonymous classes.
        // Could maybe be done by a target - it just gets built last.
    }

    public static void print(String message, Object... varargs) {
        System.out.println(String.format(message, varargs));
    }

    public static void warn(String message, Object... varargs) {
        System.err.println(String.format(message, varargs));
    }

    public static void error(String message, Object... varargs) {
        throw new RuntimeException(String.format(message, varargs));
    }

    public static void main(String args[]) {
        File root = new File(args[0]);
        Match match = new Match(root);
        match.light();
    }

}
