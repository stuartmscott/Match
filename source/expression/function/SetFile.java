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
import java.util.Map;

public class SetFile extends Set {

    private File file;
    private String filename;

    public SetFile(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        file = new File(target.getDirectory(), mValue);
        filename = file.toPath().normalize().toAbsolutePath().toString();
        if (!file.exists()) {
            mMatch.error(String.format("File %s does not exist", filename));
        }
        target.setName("SetFile:" + mKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        super.configure();
        mMatch.addFile(filename);
        mMatch.provideFile(file);
    }
}
