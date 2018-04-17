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

import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.builder.Swagger2MarkupConfigBuilder;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.integration.api.OpenApiContext;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mojo(name = "openApi2markup", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class OpenApi2MarkupMojo extends AbstractMojo {
    @Parameter(property = "openapi.resourcePackages", required = true)
    private String resourcePackages;

    @Parameter(property = "openapi.prettyPrint", defaultValue = "true")
    private boolean prettyPrint;

    @Parameter(property = "openapi.outputDirectory", defaultValue = "${project.build.directory}/generated/openapi")
    private File outputDirectory;

    @Parameter(property = "openapi.openApiFileName", defaultValue = "openApi.yaml")
    private String openApiFileName;

    @Parameter
    private Map<String, String> config = new HashMap<>();

    @Parameter(property = "openapi.skip")
    private boolean skip;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("convertSwagger2markup is skipped.");
            return;
        }

        try {
            final PluginDescriptor pluginDescriptor = (PluginDescriptor) getPluginContext().get("pluginDescriptor");
            final ClassRealm classRealm = pluginDescriptor.getClassRealm();
            final File classes = new File(project.getBuild().getOutputDirectory());
            classRealm.addURL(classes.toURI().toURL());

            final SwaggerConfiguration swaggerConfiguration =
                    new SwaggerConfiguration()
                            .prettyPrint(prettyPrint)
                            .resourcePackages(Stream.of(resourcePackages).collect(Collectors.toSet()));


            final OpenApiContext ctx = new JaxrsOpenApiContextBuilder()
                    .openApiConfiguration(swaggerConfiguration)
                    .buildContext(true);

            FileUtils.forceMkdir(this.outputDirectory);

            final File openApiFile = new File(this.outputDirectory, openApiFileName);
            FileUtils.write(openApiFile, Yaml.pretty(ctx.read()));

            final Swagger2MarkupConverter openApiConverter =
                    Swagger2MarkupConverter.from(openApiFile.toURI())
                                           .withConfig(new Swagger2MarkupConfigBuilder(config).build())
                                           .build();

            openApiConverter.toFolder(this.outputDirectory.toPath());

        } catch (final Exception e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }
}
