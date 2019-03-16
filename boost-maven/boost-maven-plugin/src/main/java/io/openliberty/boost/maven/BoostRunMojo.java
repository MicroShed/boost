package io.openliberty.boost.maven;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import io.openliberty.boost.maven.utils.BoostLogger;
import io.openliberty.boost.maven.utils.MavenProjectUtil;

@Mojo(name = "run", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class BoostRunMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true)
    protected MavenSession session;

    @Component
    protected BuildPluginManager pluginManager;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // Check our runtime and execute maven goal for either liberty-run or
        // wildfly-run
    	String runtime = MavenProjectUtil.getBoostRuntime(project, BoostLogger.getInstance());
    	BoostLogger.getInstance().info("Boost runtime is: " + runtime);
        if (runtime.equals("wildfly")) {
        	executeMojo(
                    plugin(groupId("io.openliberty.boost"), artifactId("boost-maven-plugin"),
                            version("0.1.3-SNAPSHOT")),
                    goal("wildfly-run"), configuration(), executionEnvironment(project, session, pluginManager));
        } else {
        	executeMojo(
                plugin(groupId("io.openliberty.boost"), artifactId("boost-maven-plugin"), version("0.1.3-SNAPSHOT")),
                goal("liberty-run"), configuration(), executionEnvironment(project, session, pluginManager));
        }
    }

}
