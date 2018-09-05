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
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;


@Mojo(name = "dockerize", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class DockerizeMojo extends AbstractBoostMojo{

    private static final String LIBERTY_IMAGE_1 = "open-liberty:springBoot1";
    private static final String LIBERTY_IMAGE_2 = "open-liberty:springBoot2";
    private static final String LIB_INDEX_CACHE = "lib.index.cache";
    private static final String ARG_SOURCE_APP = "--sourceAppPath";
    private static final String ARG_DEST_THIN_APP = "--targetThinAppPath";
    private static final String ARG_DEST_LIB_CACHE = "--targetLibCachePath";
    private static final String FROM = "FROM ";
    private static final String COPY = "COPY ";
    private static final String RUN = "RUN ";
   
    
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            File targetFile = getTargetFile();
            if(isFileExecutable(targetFile)) {
                createDockerFile(targetFile.getName());
            } else {
                throw new MojoExecutionException(targetFile.getCanonicalPath() +" file is not an executable archive. "
                        + "The repackage goal of the spring-boot-maven-plugin must be configured to run first in order to create the required executable archive.");
            }            
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
    
    @SuppressWarnings("resource")
    private boolean isFileExecutable(File file) throws IOException {
        if(file.exists()) {
            Manifest manifest = new JarFile(file).getManifest();
            if(manifest != null) {
                String startClass = manifest.getMainAttributes().getValue("Start-Class");
                if(startClass != null) {
                    return true;
                }
            }  
        }
        return false;        
    }

    public void createDockerFile(String targetFileName) throws IOException, FileAlreadyExistsException, MojoExecutionException {
        String libertyImage = null;
        String springBootVersion = findSpringBootVersion();
        try {
            File dockerFile = new File(projectDirectory, "Dockerfile");
            Files.createFile(dockerFile.toPath());
            getLog().info("Creating Dockerfile: " + dockerFile.getAbsolutePath());
        
            if (springBootVersion.startsWith("1.")) {
                libertyImage = LIBERTY_IMAGE_1;
            } else if (springBootVersion.startsWith("2.")) {
                libertyImage = LIBERTY_IMAGE_2;
            } else {
                throw new MojoExecutionException("No supporting docker image found for Open Liberty for the Spring Boot version " + springBootVersion);
            }
            
            writeDockerFile(libertyImage, dockerFile, targetFileName);
           
        } catch (FileAlreadyExistsException e) {
            getLog().warn("Dockerfile already exists");
        }    
    }

    private void writeDockerFile(String libertyImage, File dockerFile, String targetFileName) throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        lines.add(FROM + libertyImage + " as " + "staging");
        
        lines.add("\n");
        lines.add("# The APP_FILE ARG provides the final name of the Spring Boot application archive");
        lines.add("ARG" + " " + "APP_FILE");
        
        lines.add("\n");
        lines.add("# Stage the fat JAR");
        lines.add(COPY + outputDirectory.getName() + "/" + "${APP_FILE}" + " " + "/staging/" + "${APP_FILE}");
        
        lines.add("\n");
        lines.add("# Thin the fat application; stage the thin app output and the library cache");   
        lines.add(RUN + "springBootUtility thin " +
                ARG_SOURCE_APP     + "=" + "/staging/" + "${APP_FILE}" + " " +
                ARG_DEST_THIN_APP  + "=" + "/staging/" + "thin-${APP_FILE}" + " " +
                ARG_DEST_LIB_CACHE + "=" + "/staging/" + LIB_INDEX_CACHE);
        
        lines.add("\n");
        lines.add("# Final stage, only copying the liberty installation (includes primed caches)"); 
        lines.add("# and the lib.index.cache and thin application");    
        lines.add(FROM + libertyImage);
        
        lines.add("\n");
        lines.add("ARG" + " " + "APP_FILE");
        
        lines.add("\n");
        lines.add(COPY + "--from=staging " + "/staging/" + LIB_INDEX_CACHE +" " + "/" + LIB_INDEX_CACHE);
        
        lines.add("\n");
        lines.add(COPY + "--from=staging " + "/staging/" + "thin-${APP_FILE}" + " "+ "/config/dropins/spring/" + "thin-${APP_FILE}");
        Files.write(dockerFile.toPath(), lines, Charset.forName("UTF-8"));

    }
}
