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

package io.openliberty.boost.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.openliberty.boost.common.BoostLoggerI;

public class BoostUtil {
	
    public static boolean isNotNullOrEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    public static boolean isLibertyJar(File artifact, BoostLoggerI logger) {
        boolean isLibertyJar = false;

        try (JarFile artifactJar = new JarFile(artifact)) {
            isLibertyJar = artifactJar.getEntry("wlp") != null;
        } catch (Exception e) {
            logger.debug("Exception when checking Liberty JAR", e);
        }

        return isLibertyJar;
    }
    
    public static void extract(File artifact, File projectDirectory) {
        File extractDir = new File(projectDirectory.getPath() + "/target/dependency/");
        if (!extractDir.exists())
            extractDir.mkdirs();

        try {
            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(artifact));
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                File newFile = new File(extractDir, ze.getName());
                if (ze.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                ze = zis.getNextEntry();
            }
            // close last ZipEntry
            zis.closeEntry();
            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
