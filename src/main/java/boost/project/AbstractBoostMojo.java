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
package boost.project;

import java.io.File;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class AbstractBoostMojo extends AbstractMojo{
    @Parameter(defaultValue = "${project}", required = true)
    protected MavenProject mavenProject;
    
    @Parameter(defaultValue = "${session}")
    protected MavenSession mavenSession;

    @Component
    protected BuildPluginManager pluginManager;
    
    /**
     * Directory containing the generated archive.
     * 
     * @since 1.0
     */
    @Parameter(defaultValue = "${project.basedir}", required = true)
    protected File projectDirectory;

    /**
     * Directory containing the generated archive.
     * 
     * @since 1.0
     */
    @Parameter(defaultValue = "${project.build.directory}", required = true)
    protected File outputDirectory;
    
    /**
     * Name of the generated archive.
     * 
     * @since 1.0
     */
    @Parameter(defaultValue = "${project.build.finalName}", required = true)
    protected String finalName;
    
    /**
     * Classifier to add to the artifact generated. If given, the classifier will be
     * suffixed to the artifact and the main artifact will be deployed as the
     * main artifact. If this is not given (default), it will replace the main
     * artifact and only the repackaged artifact will be deployed. Attaching the
     * artifact allows to deploy it alongside to the original one, see <a href=
     * "http://maven.apache.org/plugins/maven-deploy-plugin/examples/deploying-with-classifiers.html"
     * > the maven documentation for more details</a>.
     * 
     * @since 1.0
     */
    @Parameter
    protected String classifier;
    
    
    protected File getTargetFile() {
        String classifier = (this.classifier == null ? "" : this.classifier.trim());
        if (!classifier.isEmpty() && !classifier.startsWith("-")) {
            classifier = "-" + classifier;
        }
        if (!this.outputDirectory.exists()) {
            this.outputDirectory.mkdirs();
        }
        return new File(this.outputDirectory,
                this.finalName + classifier + "." + this.mavenProject.getArtifact().getArtifactHandler().getExtension());
    }
    
    protected String findSpringBootVersion() {
        Set<Artifact> artifacts = mavenProject.getArtifacts();
        if(artifacts != null) {           
            for(Artifact artifact : artifacts) {
                if("org.springframework.boot".equals(artifact.getGroupId()) && "spring-boot".equals(artifact.getArtifactId())){
                    return  artifact.getVersion();
                }
            }
        }
        return null;
    }
}
