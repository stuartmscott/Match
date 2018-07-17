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

package match;

import config.Config;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import match.frontend.Category;
import match.frontend.Lexem;
import match.frontend.Lexer;
import match.frontend.Parser;

/**
 * A lightweight, fast and extensible build system.
 */
public class Match implements IMatch {

    public static final String MATCH = "match";

    public static final int ERROR = -1;

    public static final List<Lexem> LEXEMS = Arrays.asList(
            new Lexem(Category.NEWLINE, "\n"),
            new Lexem(Category.WHITESPACE, "\\s"),
            new Lexem(Category.ASSIGN, "="),
            new Lexem(Category.COMMENT, "#.*\n"),
            new Lexem(Category.ORB, "\\("),
            new Lexem(Category.CRB, "\\)"),
            new Lexem(Category.OSB, "\\["),
            new Lexem(Category.CSB, "\\]"),
            new Lexem(Category.STRING_LITERAL, "\".*\""),
            new Lexem(Category.UPPER_CASE, "[A-Z][-a-zA-Z0-9]*"),
            new Lexem(Category.LOWER_CASE, "[a-z][-a-zA-Z0-9]*")
    );

    private final Config config;
    private final File root;
    private final File libraries;
    private final boolean clean;
    private final boolean quiet;
    private final boolean verbose;
    private final Map<String, CountDownLatch> files = new ConcurrentHashMap<>();
    private final List<File> matchFiles = new ArrayList<>();
    private final List<File> allFiles = new ArrayList<>();

    /**
     * Creates a new Match instance with the given config.
     */
    public Match(Config config) {
        this.config = config;
        root = new File(config.get("root"));
        libraries = new File(config.has("libraries") ? config.get("libraries") : System.getProperty("java.io.tmpdir"));
        clean = config.getBoolean("clean");
        quiet = config.getBoolean("quiet");
        verbose = config.getBoolean("verbose");
        // TODO exec targets to allow supporting custom commands, or add AndroidGradle and AndroidAnt functions to build with gradle or ant resp.
        // TODO parallel builds
        // TODO incremental builds
        // TODO building select targets vs all
        // TODO function to create distributions
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getRootDir() {
        return root;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getLibrariesDir() {
        return libraries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCleaning() {
        return clean;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isQuiet() {
        return quiet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isVerbose() {
        return verbose;
    }

    List<File> getAllFiles() {
        return allFiles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasProperty(String key) {
        return config.has(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProperty(String key) {
        String property = config.get(key);
        if (property == null) {
            error(String.format("no targets set property %s", key));
        }
        return property;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperty(String key, String value) {
        config.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFile(String file) {
        files.put(file, new CountDownLatch(1));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDirectory(File directory) {
        for (File child : directory.listFiles()) {
            if (child.isDirectory()) {
                addDirectory(child);
            } else {
                addScannedFile(child);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void provideFile(File file) {
        String filename = file.toPath().normalize().toAbsolutePath().toString();
        if (!file.exists()) {
            error(String.format("provideFile called with non-existant file %s", filename));
        }
        if (file.isDirectory()) {
            error(String.format("provideFile called with directory %s", filename));
        }
        CountDownLatch latch = files.get(filename);
        if (latch == null) {
            /*
            for (String f : files.keySet()) {
                System.out.println("File " + f);
            }
            for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
                System.out.println("Stack " + e);
            }*/
            error(String.format("provideFile called before addFile for %s", filename));
        }
        latch.countDown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void awaitFile(String file) {
        CountDownLatch latch = files.get(file);
        if (latch == null) {
            /*
            for (String f : files.keySet()) {
                System.out.println("File " + f);
            }
            for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
                System.out.println("Stack " + e);
            }*/
            error(String.format("no targets provided %s", file));
        }
        try {
            long maxWaitTimeMins = 3; // 3 minutes
            if (config.has("max-wait-time")) {
                maxWaitTimeMins = (long) config.getNumber("max-wait-time");
            }
            if (!latch.await(maxWaitTimeMins, TimeUnit.MINUTES)) {
                error(file + " took too long (> " + maxWaitTimeMins + "mins)");
            }
        } catch (InterruptedException e) {
            error("await interrupted");
        }
    }

    private void scanRoot(File root) {
        for (File child : root.listFiles()) {
            String name = child.getName();
            if (!name.matches("\\..*") && !name.equals("out")) {
                if (child.isDirectory()) {
                    loadFiles(child);
                } else {
                    addScannedFile(child);
                }
            }
        }
    }

    private void loadFiles(File directory) {
        for (File child : directory.listFiles()) {
            if (child.isDirectory()) {
                loadFiles(child);
            } else {
                addScannedFile(child);
            }
        }
    }

    private void addScannedFile(File file) {
        allFiles.add(file);
        if (file.getName().equals(MATCH)) {
            matchFiles.add(file);
        }
    }

    void light() {
        long start = System.currentTimeMillis();
        println("Scanning");
        scanRoot(root);
        println("Matches: " + matchFiles);
        println("Parsing");
        List<ITarget> targets = new ArrayList<ITarget>();
        for (File match : matchFiles) {
            Lexer lexer = new Lexer(this, LEXEMS, match);
            Parser parser = new Parser(this, lexer);
            targets.addAll(parser.parse());
        }
        println("Targets: " + targets);
        println("Configuring");
        // Create a thread for each target, but only start a thread if the number of targets that
        // aren't blocked is under MAX_THREADS. If all targets are blocked there is a deadlock.
        for (File file : allFiles) {
            addFile(file.toPath().normalize().toAbsolutePath().toString());
            provideFile(file);
        }
        for (ITarget target : targets) {
            try {
                target.configure();
            } catch (Exception e) {
                error(target.getName() + " failed to configure: " + e.getMessage());
            }
        }
        println("Building");
        CountDownLatch latch = new CountDownLatch(targets.size());
        for (ITarget target : targets) {
            new BuildThread(target, latch).start();
        }
        try {
            long maxBuildTimeMins = 5;// 5 minutes
            if (config.has("max-build-time")) {
                maxBuildTimeMins = (long) config.getNumber("max-build-time");
            }
            if (!latch.await(maxBuildTimeMins, TimeUnit.MINUTES)) {
                for (ITarget target : targets) {
                    if (!target.isBuilt()) {
                        println(target + " still running " + target.getLastCommand());
                    }
                }
                error("build took too long (> " + maxBuildTimeMins + "mins)");
            }
        } catch (InterruptedException e) {
            error("build interrupted");
        }
        long delta = (System.currentTimeMillis() - start) / 1000;
        long hours = delta / 3600;
        long minutes = (delta % 3600) / 60;
        long seconds = (delta % 60);
        String message = null;
        if (hours > 0) {
            message = String.format("Done %dh:%02dm:%02ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            message = String.format("Done %dm:%02ds", minutes, seconds);
        } else {
            message = String.format("Done %ds", seconds);
        }
        println(message);
        // Create a thread for each target, but only start a thread if the number of targets that
        // aren't blocked is under MAX_THREADS. If all targets are blocked there is a deadlock.
        // Look at the output files of a target and all the files under the output directory,
        // delete files that were created in the last build but is no longer made by any targets.
        // This means all targets have to know their output files even if they dont need to build.
        // This is difficult for java compiles because you cannot know beforehand, given source
        // files, which classes will get generated because of inner/anonymous classes.
        // Could maybe be done by a target - it just gets built last.
        // TODO if (isCleaning()) delete all generated files
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(String message) {
        println(String.format("warning: %s", message));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void println(String message) {
        if (!quiet) {
            System.out.println(message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(String message) {
        System.err.println(message);
        System.exit(ERROR);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(Exception exception) {
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        error(sw.toString());
    }

    /**
     * Creates and ignites a Match instance using a config file from ~/match/config.
     */
    public static void main(String[] args) {
        File[] files = { new File(System.getProperty("user.home") + "/match/config") };
        Match match = new Match(Config.create(args, files));
        match.light();
    }

    private static class BuildThread extends Thread {

        private final ITarget target;
        private final CountDownLatch latch;

        private BuildThread(ITarget target, CountDownLatch latch) {
            this.target = target;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                target.build();
            } finally {
                latch.countDown();
            }
        }
    }
}
