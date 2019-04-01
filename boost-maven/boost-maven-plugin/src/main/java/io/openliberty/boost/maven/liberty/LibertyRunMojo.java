/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package io.openliberty.boost.maven.liberty;

import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import io.openliberty.boost.common.config.ConfigConstants;

/**
 * Runs the executable archive application (in the console foreground).
 */
@Mojo(name = "run", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class LibertyRunMojo extends AbstractLibertyMojo {

    /**
     * Clean all cached information on server start up.
     */
    @Parameter(property = "clean", defaultValue = "false")
    private boolean clean;

    @Override
    public void execute() throws MojoExecutionException {
        super.execute();
        
        if (this.targetRuntime.equals(ConfigConstants.TOMEE_RUNTIME)) {
            runTomee();
        } else {
            runLiberty();
        }
    }
    
    private void runLiberty() throws MojoExecutionException {
        executeMojo(getPlugin(), goal("run"),
                configuration(element(name("serverName"), libertyServerName),
                        element(name("clean"), String.valueOf(clean)), getRuntimeArtifactElement()),
                getExecutionEnvironment());
    }
    
    private void runTomee() throws MojoExecutionException {
        executeMojo(getTOMEEPlugin(), goal("run"),
                configuration(element(name("tomeeAlreadyInstalled"), "true"), element(name("context"), "ROOT"), element(name("tomeeVersion"), "8.0.0-M2"), element(name("tomeeClassifier"), "plus")),
                getExecutionEnvironment());
    }

}
