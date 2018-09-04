package io.openliberty.boost.liberty;

import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;

import org.apache.maven.plugin.MojoExecutionException;

public class LibertyStopMojo extends AbstractLibertyMojo {

    @Override
    public void execute() throws MojoExecutionException {
        executeMojo(getPlugin(), goal("stop"), null, getExecutionEnvironment());
    }

}