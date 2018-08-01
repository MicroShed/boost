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

import java.io.File;

import org.junit.Test;
import org.codehaus.plexus.util.FileUtils;

public class FeatureVersionIT {
	
	private static String SOURCE_FEATURE_LIST_XML = "src/test/resources/featureList.xml";
	private static String TARGET_FEATURE_LIST_XML = "target/liberty/wlp/usr/servers/BoostServer/configDropins/overrides/featureList.xml";
    
    @Test
    public void testFeatureVersion() throws Exception {
    	File targetFile = new File(TARGET_FEATURE_LIST_XML);
    	assertTrue(targetFile.getCanonicalFile() + "does not exist.", targetFile.exists());
    	
    	// Check contents of file for springBoot-15 feature
    	File sourceFile = new File(SOURCE_FEATURE_LIST_XML);
    	
    	assertEquals("verify target server featureList.xml", FileUtils.fileRead(sourceFile),
                FileUtils.fileRead(targetFile));
    }
}
