package io.openliberty.boost.liberty;

import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "stop")
public class LibertyStopMojo extends AbstractLibertyMojo {

    @Override
    public void execute() throws MojoExecutionException {
        executeMojo(getPlugin(), goal("stop"), 
                configuration(
                        element(name("serverName"), libertyServerName)
                ), getExecutionEnvironment());
    }

}