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

package boost.project.utils;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import boost.project.BoostException;

public class BoostUtil {

    public static boolean isLibertyJar(File artifact) throws BoostException, IOException {
        boolean isLibertyJar;
        JarFile artifactJar;
        
        try {
            artifactJar = new JarFile(artifact);
            isLibertyJar =  artifactJar.getEntry("wlp") != null;
        } catch (Exception e) {
            throw new BoostException("Error checking Liberty artifact.", e);
        }
        
        if(artifactJar != null) {
            artifactJar.close();
        }
        
        return isLibertyJar;
    }
    
}
