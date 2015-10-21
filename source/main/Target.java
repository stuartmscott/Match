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

import expression.function.IFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Target implements ITarget {

    private Map<String, String> mProperties = new HashMap<String, String>();
    private List<String> mInputs = new ArrayList<String>();
    private List<String> mOutputs = new ArrayList<String>();

    private IMatch mMatch;
    private IFunction mFunction;

    public Target(IMatch match) {
        mMatch = match;
    }

    /**
     * {inheritDoc}
     */
    public void setFunction(IFunction function) {
        mFunction = function;
    }

    /**
     * {inheritDoc}
     */
    public String getProperty(String key) {
        String property = null;
        do {
            // Gets the property for the given key from this target.
            property = mProperties.get(key);
            if (property != null) {
                return property;
            }
            // The property is not in the Target, try looking in the match.
            property = mMatch.getProperty(key);
            if (property != null) {
                return property;
            }
            // If the property is still null then wait.
            try {
                mMatch.wait();
            } catch (InterruptedException e) {}
        } while (property == null);
        return property;
    }

    /**
     * {inheritDoc}
     */
    public void setProperty(String key, String value) {
        mProperties.put(key, value);
        // Notify everyone who may have been waiting on this.
        mMatch.notifyAll();
    }

    /**
     * {inheritDoc}
     */
    public void build() {
        mFunction.resolve();
        // TODO put this target's input and output files in the database
    }
}
