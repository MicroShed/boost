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

public class FeatureVersionIT {
	
	private static final String SPRING_BOOT_15_FEATURE = "<feature>springBoot-1.5</feature>";
	private static String TARGET_FEATURE_LIST_XML = "target/liberty/wlp/usr/servers/BoostServer/configDropins/overrides/featureList.xml";
    
    @Test
    public void testFeatureVersion() throws Exception {
    	File targetFile = new File(TARGET_FEATURE_LIST_XML);
    	assertTrue(targetFile.getCanonicalFile() + "does not exist.", targetFile.exists());
    	
    	// Check contents of file for springBoot-15 feature
    	boolean found = false;
    	BufferedReader br = new BufferedReader(new FileReader(TARGET_FEATURE_LIST_XML));
    	String line;
    	while ((line = br.readLine()) != null) {
    	    if (line.contains(SPRING_BOOT_15_FEATURE)) {
    	    	found = true;
    	    	break;
    	    }
    	}
    	
    	assertTrue("The "+SPRING_BOOT_15_FEATURE+" feature was not found in the server configuration", found);    
    }
}
