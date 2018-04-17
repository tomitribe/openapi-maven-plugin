/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.maven.plugin.openapi;

import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Ignore
public class OpenApi2MarkupMojoTest {
    @Rule
    public MojoRule rule = new MojoRule() {
        @Override
        protected void before() throws Throwable {
        }

        @Override
        protected void after() {
        }
    };


    @Test
    public void openApiMojo() throws Exception {
        final File pom = new File("target/test-classes/openapi");
        assertNotNull(pom);
        assertTrue(pom.exists());

        final OpenApi2MarkupMojo mojo = (OpenApi2MarkupMojo) rule.lookupConfiguredMojo(pom, "openApi2markup");
        assertNotNull(mojo);

        mojo.execute();

        final File outputDirectory = (File) rule.getVariableValueFromObject(mojo, "outputDirectory");
        assertNotNull(outputDirectory);
        final String openApiFileName = (String) rule.getVariableValueFromObject(mojo, "openApiFileName");
        assertNotNull(openApiFileName);

        final File openApiFile = new File(outputDirectory, openApiFileName);
        assertTrue(openApiFile.exists());
    }

}
