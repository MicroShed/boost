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
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.SpringBootVersion;

public class DockerizeIT {
    
    private static File dockerFile;
    
    @BeforeClass
    public static void setup() throws Exception {
        dockerFile = new File("Dockerfile");
    }
    
    @Test
    public void testDockerizeCreatesDockerfile() throws Exception {
        assertTrue(dockerFile.getCanonicalPath() + " was not created", dockerFile.exists());
    }
    
    @Test
    public void testDockerfileContainsCorrectLibertyImage() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(dockerFile));
        String line = reader.readLine();
        String version = SpringBootVersion.getVersion();
        if(version !=null) {
            if(version.startsWith("1.")) {
                assertTrue("Expected Open liberty base image open-liberty:springBoot1 was not found in " + dockerFile.getCanonicalPath(), line.contains("open-liberty:springBoot1"));
            } else if (version.startsWith("2.")) {
                assertTrue("Expected Open liberty base image open-liberty:springBoot2 was not found in " + dockerFile.getCanonicalPath(), line.contains("open-liberty:springBoot2"));
            }
        }
    }
}