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

package io.openliberty.boost.utils;

import java.io.File;
import java.util.jar.JarFile;

public class BoostUtil {

    public static boolean isLibertyJar(File artifact) {
        boolean isLibertyJar = false;
        
        try(JarFile artifactJar = new JarFile(artifact)) {
            isLibertyJar =  artifactJar.getEntry("wlp") != null;
        } catch (Exception e) {}

        return isLibertyJar;
    }
    
}
