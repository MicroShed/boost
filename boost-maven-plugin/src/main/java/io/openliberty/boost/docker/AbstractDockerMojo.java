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
package io.openliberty.boost.docker;

import java.io.File;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.crypto.SettingsDecrypter;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerConfigReader;
import com.spotify.docker.client.ImageRef;
import com.spotify.docker.client.auth.ConfigFileRegistryAuthSupplier;
import com.spotify.docker.client.auth.RegistryAuthSupplier;
import com.spotify.docker.client.exceptions.DockerCertificateException;

public abstract class AbstractDockerMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true)
    protected MavenSession session;

    /**
     * The settings decrypter.
     */
    @Component
    private SettingsDecrypter settingsDecrypter;

    /**
     * Directory containing the generated archive.
     * 
     * @since 1.0
     */
    @Parameter(defaultValue = "${project.basedir}", required = true, readonly = true)
    protected File projectDirectory;

    /**
     * Directory containing the generated archive.
     * 
     * @since 1.0
     */
    @Parameter(defaultValue = "${project.build.directory}", required = true, readonly = true)
    protected File outputDirectory;

    /**
     * Name of the generated archive.
     * 
     * @since 1.0
     */
    @Parameter(defaultValue = "${project.build.finalName}", required = true, readonly = true)
    protected String finalName;

    /**
     * Classifier to add to the artifact generated. If given, the classifier will be
     * suffixed to the artifact and the main artifact will be deployed as the main
     * artifact. If this is not given (default), it will replace the main artifact
     * and only the repackaged artifact will be deployed. Attaching the artifact
     * allows to deploy it alongside to the original one, see <a href=
     * "http://maven.apache.org/plugins/maven-deploy-plugin/examples/deploying-with-classifiers.html"
     * > the maven documentation for more details</a>.
     * 
     * @since 1.0
     */
    @Parameter
    protected String classifier;

    /**
     * The repository to put the built image into, <tt>${project.artifactId}</tt> is
     * used by default. You should also set the <tt>tag</tt> parameter, otherwise
     * the tag <tt>latest</tt> is used by default.
     */
    @Parameter(property = "repository", defaultValue = "${project.artifactId}")
    protected String repository;

    /**
     * The tag to apply to the built image. <tt>latest</tt> is used by default.
     */
    @Parameter(property = "tag", defaultValue = "latest")
    protected String tag;

    /**
     * Path to docker config file, if the default is not acceptable.
     */
    @Parameter(property = "dockerConfigFile")
    protected File dockerConfigFile;

    /**
     * Connect to Docker Daemon using HTTP proxy, if set.
     */
    @Parameter(defaultValue = "true", property = "useProxy")
    protected boolean useProxy;

    @Parameter(defaultValue = "300000" /* 5 minutes */, property = "readTimeoutMillis", required = true)
    protected long readTimeoutMillis;

    @Parameter(defaultValue = "300000" /* 5 minutes */, property = "connectTimeoutMillis", required = true)
    protected long connectTimeoutMillis;

    protected Log log;

    protected abstract void execute(DockerClient dockerClient) throws MojoExecutionException, MojoFailureException;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        log = getLog();
        execute(getDockerClient());
    }

    private DockerClient getDockerClient() throws MojoExecutionException {
        final RegistryAuthSupplier authSupplier = createRegistryAuthSupplier();
        try {
            return DefaultDockerClient.fromEnv().readTimeoutMillis(readTimeoutMillis)
                    .connectTimeoutMillis(connectTimeoutMillis).registryAuthSupplier(authSupplier).useProxy(useProxy)
                    .build();
        } catch (DockerCertificateException e) {
            throw new MojoExecutionException("Problem loading Docker certificates", e);
        }
    }

    private RegistryAuthSupplier createRegistryAuthSupplier() {
        final RegistryAuthSupplier supplier;
        final ImageRef ref = new ImageRef(getImageName());
        final Settings settings = session.getSettings();
        final Server server = settings.getServer(ref.getRegistryName());

        //Check for the registry credentials in maven settings.xml and in default config path
        if (server != null) {
            supplier = new MavenSettingsAuthSupplier(server, settings, settingsDecrypter, log);
        } else if (dockerConfigFile == null || "".equals(dockerConfigFile.getName())) {
            supplier = new ConfigFileRegistryAuthSupplier();
        } else {
            supplier = new ConfigFileRegistryAuthSupplier(new DockerConfigReader(), dockerConfigFile.toPath());
        }
        return supplier;
    }

    protected File getAppArchive() {
        String classifier = (this.classifier == null ? "" : this.classifier.trim());
        if (!classifier.isEmpty() && !classifier.startsWith("-")) {
            classifier = "-" + classifier;
        }
        if (!this.outputDirectory.exists()) {
            this.outputDirectory.mkdirs();
        }
        return new File(this.outputDirectory,
                this.finalName + classifier + "." + this.project.getArtifact().getArtifactHandler().getExtension());
    }

    protected final String getImageName() {
        return this.repository + ":" + this.tag;
    }

    protected String getImageName(String repository, String tag) {
        return repository + ":" + tag;
    }
}
