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
import org.apache.maven.plugins.annotations.ResolutionScope;

import io.openliberty.boost.common.config.ConfigConstants;

/**
 * Stops the executable archive application started by the 'start' or 'run'
 * goals.
 */
@Mojo(name = "stop", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class LibertyStopMojo extends AbstractLibertyMojo {

    @Override
    public void execute() throws MojoExecutionException {
        super.execute();

        if (this.targetRuntime.equals(ConfigConstants.TOMEE_RUNTIME)) {
            stopTomee();
        } else {
            stopLiberty();
        }
    }
    
    private void stopLiberty() throws MojoExecutionException {
        executeMojo(getPlugin(), goal("stop"),
                configuration(element(name("serverName"), libertyServerName), getRuntimeArtifactElement()),
                getExecutionEnvironment());
    }
    
    private void stopTomee() throws MojoExecutionException {
        executeMojo(getTOMEEPlugin(), goal("stop"),
                configuration(element(name("tomeeAlreadyInstalled"), "true"), element(name("context"), "ROOT"), element(name("tomeeVersion"), "8.0.0-M2"), element(name("tomeeClassifier"), "plus")),
                getExecutionEnvironment());
    }

}
