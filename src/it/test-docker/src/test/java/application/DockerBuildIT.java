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

package application;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.junit.Test;
import org.junit.BeforeClass;


public class DockerBuildIT {
    
    @Test
    public void testDefaultRepositoryName() throws Exception {
        File repository = new File("target/docker/repository");
        if(repository != null && repository.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(repository));
            String line = reader.readLine();
            assertEquals("Expected default Docker Repository ${project.artifactId}  was not created", "test-docker", line);
        }
    }
    
    @Test
    public void testDefaultTag() throws Exception {
        File tag = new File("target/docker/tag");
        if(tag != null && tag.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(tag));
            String line = reader.readLine();
            assertEquals("Expected default Docker Tag ${project.version} was not created", "1.0.0", line);
        }
    }
    
    @Test
    public void testDefaultImageName() throws Exception {
        File imageName = new File("target/docker/image-name");
        if(imageName != null && imageName.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(imageName));
            String line = reader.readLine();
            assertEquals("Expected default Docker Image ${project.artifactId}:${project.version} was not created", "test-docker:1.0.0", line);
        }
    }
}