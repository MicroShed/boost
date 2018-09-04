package io.openliberty.boost.liberty;

import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

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
        executeMojo(getPlugin(), goal("start"),
                configuration(
                        element(name("serverName"), libertyServerName),
                        element(name("verifyTimeout"), String.valueOf(verifyTimeout)),
                        element(name("serverStartTimeout"), String.valueOf(serverStartTimeout)),
                        element(name("clean"), String.valueOf(clean))
                ),
                getExecutionEnvironment());
    }

}