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

/**
 * Starts the executable archive application as a background process.
 * 
 */
@Mojo(name = "start")
public class LibertyStartMojo extends AbstractLibertyMojo {

    /**
     * Time in seconds to wait while verifying that the server has started.
     */
    @Parameter(property = "verifyTimeout", defaultValue = "30")
    private int verifyTimeout = 30;

    /**
     * Time in seconds to wait while verifying that the server has started.
     */
    @Parameter(property = "serverStartTimeout", defaultValue = "30")
    private int serverStartTimeout = 30;

    /**
     * Clean all cached information on server start up.
     */
    @Parameter(property = "clean", defaultValue = "false")
    private boolean clean;

    @Override
    public void execute() throws MojoExecutionException {
        super.execute();

        executeMojo(getPlugin(), goal("start"),
                configuration(element(name("serverName"), libertyServerName),
                        element(name("verifyTimeout"), String.valueOf(verifyTimeout)),
                        element(name("serverStartTimeout"), String.valueOf(serverStartTimeout)),
                        element(name("clean"), String.valueOf(clean)), getRuntimeArtifactElement()),
                getExecutionEnvironment());
    }

}
