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

public class DerbyVersionIT {

    private static final String DERBY_JAR = "target/liberty/wlp/usr/servers/BoostServer/resources/derby-10.11.1.1.jar";

    /**
     * Test that the release project is using the Derby version that it defined
     * in its own project
     * 
     * @throws Exception
     */
    @Test
    public void testDerbyVersion() throws Exception {
        File targetFile = new File(DERBY_JAR);
        assertTrue(targetFile.getCanonicalFile() + "does not exist.", targetFile.exists());
    }
}
