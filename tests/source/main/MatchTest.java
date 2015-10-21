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
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class MatchTest {

    @Test
    public void loadFiles() throws IOException {
        File root = createFileStructure();
        Match match = new Match(root);
        match.light();
        Assert.assertEquals("Wrong number of files", 4, match.getAllFiles().size());
    }

    public static File createFileStructure() throws IOException {
        File root = File.createTempFile("temp", Long.toString(System.currentTimeMillis())); 
        root.delete();
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

    public static void deleteFileStructure(File directory) {
        for (File child : directory.listFiles()) {
            if (child.isDirectory()) {
                deleteFileStructure(child);
            } else {
                child.delete();
            }
        }
    }
}
