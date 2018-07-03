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

import expression.function.IFunction;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Target implements ITarget {

    private IMatch mMatch;
    private File mFile;
    private String mName = "";
    private IFunction mFunction;
    private String mLastCommand;
    private volatile boolean mFinished = false;

    public Target(IMatch match, File file) {
        mMatch = match;
        mFile = file;
    }

    /**
     * {inheritDoc}
     */
    @Override
    public String getName() {
        return mName;
    }

    /**
     * {inheritDoc}
     */
    @Override
    public void setName(String name) {
        mName = name;
    }

    /**
     * {inheritDoc}
     */
    @Override
    public File getFile() {
        return mFile;
    }

    /**
     * {inheritDoc}
     */
    @Override
    public File getDirectory() {
        return mFile.getParentFile();
    }

    /**
     * {inheritDoc}
     */
    @Override
    public void setFunction(IFunction function) {
        mFunction = function;
    }

    /**
     * {inheritDoc}
     */
    @Override
    public void configure() {
        mFunction.configure();
    }

    /**
     * {inheritDoc}
     */
    @Override
    public void build() {
        try {
            mFunction.resolve();
            // TODO put this target's input and output files in the database
        } finally {
            mFinished = true;
        }
    }

    /**
     * {inheritDoc}
     */
    @Override
    public boolean isBuilt() {
        return mFinished;
    }

    /**
     * {inheritDoc}
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * {inheritDoc}
     */
    @Override
    public int runCommand(String command) {
        int result = 0;
        try {
            mLastCommand = command;
            // mMatch.println(command);
            ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", command);
            pb.directory(getDirectory());
            Process process = pb.start();
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            result = process.waitFor();
            String line = "";
            boolean loop = true;
            while (loop) {
                loop = false;
                if ((line = input.readLine()) != null) {
                    mMatch.println(line);
                    loop = true;
                }
                if ((line = error.readLine()) != null) {
                    if (result != 0) {
                        mMatch.println(String.format("error: %s", line));
                    }
                    loop = true;
                }
            }
            if (result != 0) {
                mMatch.error("error: " + command);
            }
        } catch (Exception e) {
            mMatch.error(e);
        }
        return result;
    }

    /**
     * {inheritDoc}
     */
    @Override
    public String getLastCommand() {
        return mLastCommand;
    }
}
