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
import java.util.Map;

import match.IMatch;
import match.ITarget;
import match.expression.IExpression;
import match.expression.Literal;

/**
 * Sets up the given file in the build.
 */
public class SetFile extends Set {

    private File file;
    private String filename;

    /**
     * Initializes the function with the given parameters.
     */
    public SetFile(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        file = new File(target.getDirectory(), value);
        filename = file.toPath().normalize().toAbsolutePath().toString();
        if (!file.exists()) {
            match.error(String.format("File %s does not exist", filename));
        }
        target.setName("SetFile:" + key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        super.configure();
        match.addFile(filename);
        match.provideFile(file);
    }
}
