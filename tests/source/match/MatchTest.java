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
import java.io.IOException;
import java.security.Permission;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class MatchTest {

    private static final String FOO = "foo";
    private static final String BAR = "bar";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    public File root;
    public Config config;

    /**
     * Sets up the file structure, config, and security manager for the tests.
     */
    @Before
    public void setUp() throws IOException {
        root = folder.getRoot();
        createFileStructure(root);
        config = new Config();
        config.put("root", root.toPath().toString());
        System.setSecurityManager(new ExitSecurityManager());
    }

    /**
     * Tears down the security manager used by the tests.
     */
    @After
    public void tearDown() throws Exception {
        System.setSecurityManager(null);
    }

    /**
     * Tests the handling properties.
     */
    @Test
    public void properties() throws Exception {
        Match match = createMatch(config);
        try {
            match.getProperty(FOO);
            Assert.fail("Match should fail if property is not set");
        } catch (ExitException e) {
            /* ignored */
        }
        match.setProperty(FOO, BAR);
        Assert.assertEquals("Wrong property", BAR, match.getProperty(FOO));
    }

    /**
     * Tests the handling of files.
     */
    @Test
    public void files() throws Exception {
        Match match = createMatch(config);
        File file = folder.newFile(FOO);
        String fileName = file.toPath().toString();
        match.addFile(fileName);
        Worker worker = new Worker(match, fileName);
        worker.start();
        worker.await();
        // Worker should wait until the file is provided
        Assert.assertFalse("Worker should not have ended", worker.ended);
        match.provideFile(file);
        worker.join();
        Assert.assertTrue("Worker should have ended", worker.ended);
    }

    /**
     * Test errors are generated if a file is provided or awaited before being added.
     */
    @Test
    public void files_noAdd() throws Exception {
        Match match = createMatch(config);
        try {
            // Provide a file that wasn't added
            match.provideFile(new File(FOO));
            Assert.fail("Match should fail if file wasn't added");
        } catch (Exception e) {
            /* ignored */
        }
        try {
            // Await on a file that wasn't added
            match.awaitFile(FOO);
            Assert.fail("Match should fail if file wasn't added");
        } catch (Exception e) {
            /* ignored */
        }
    }

    /**
     * Tests correct number of files are loaded.
     */
    @Test
    public void loadFiles() throws Exception {
        Match match = createMatch(config);
        match.light();
        Assert.assertEquals("Wrong number of files", 4, match.getAllFiles().size());
    }

    private Match createMatch(Config config) {
        config.put("quiet");
        return new Match(config);
    }

    /**
     * Utility to create file structure.
     *
     * <p>
     * a
     * -b
     * c
     * -d
     * --e
     * --f
     * bar
     * </p>
     */
    public static File createFileStructure(File root) throws IOException {
        root.mkdirs();
        Assert.assertTrue("Root doesn't exist", root.exists());
        Assert.assertTrue("Root isn't a directory", root.isDirectory());
        File a = new File(root, "a");
        a.mkdirs();
        File b = new File(a, "b");
        b.createNewFile();
        File c = new File(root, "c");
        c.mkdirs();
        File d = new File(c, "d");
        d.mkdirs();
        File e = new File(d, "e");
        e.createNewFile();
        File f = new File(d, "f");
        f.createNewFile();
        File bar = new File(root, "bar");
        bar.createNewFile();
        return root;
    }

    private static class ExitException extends SecurityException {

        public static final long serialVersionUID = -1;

        final int status;

        ExitException(int status) {
            super("ExitException");
            this.status = status;
        }
    }

    private static class ExitSecurityManager extends SecurityManager {

        @Override
        public void checkPermission(Permission permission) {}

        @Override
        public void checkPermission(Permission permission, Object context) {}

        @Override
        public void checkExit(int status) {
            super.checkExit(status);
            throw new ExitException(status);
        }
    }

    private static class Worker extends Thread {

        private Match match;
        private String fileName;
        private CountDownLatch latch = new CountDownLatch(1);
        private volatile boolean ended = false;

        Worker(Match match, String fileName) {
            this.match = match;
            this.fileName = fileName;
        }

        @Override
        public void run() {
            latch.countDown();
            match.awaitFile(fileName);
            ended = true;
        }

        void await() {
            try {
                latch.await();
            } catch (InterruptedException e) {
                /* ignored */
            }
        }
    }
}
