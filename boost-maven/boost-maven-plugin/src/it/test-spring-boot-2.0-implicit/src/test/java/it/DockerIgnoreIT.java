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
package it;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.junit.Test;

public class DockerIgnoreIT {

    @Test
    public void testPluginAppendsNewLinesInExistingDockerIgnore() throws Exception {
        File dockerignore = new File(".dockerignore");
        assertTrue(".dockerignore does not exists", dockerignore.exists());
        try (FileReader fileReader = new FileReader(dockerignore)) {
            try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                String line = bufferedReader.readLine();
                assertNotNull(".dockerignore cannot be empty", line);
                assertEquals("Existing comment not found", "# This is a test dockerignore", line);
                line = bufferedReader.readLine();
                assertEquals("Empty line not found", "", line);
                line = bufferedReader.readLine();
                assertEquals("Expected comment not found", "# The following lines are added by boost-maven-plugin", line);
            }
        }
    }
}
