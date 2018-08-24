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

import static io.openliberty.boost.utils.ConfigConstants.BOOT_VERSION_ATTRIBUTE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.io.FileUtils;

import io.openliberty.boost.BoostException;

public class SpringBootUtil {
    
    private boolean isSpringBootUberJar(File artifact) throws IOException {
        try (JarFile jarFile = new JarFile(artifact)) {
            Manifest manifest = jarFile.getManifest();
            return (manifest != null && manifest.getMainAttributes()
                    .getValue(BOOT_VERSION_ATTRIBUTE) != null);
        }
    }
    
    public String getSpringBootUberJarPath(File artifact) throws BoostException {
        try {
            return artifact.getCanonicalFile().getPath() + ".spring";
        } catch (IOException e) {
            throw new BoostException("Error getting Spring Boot uber JAR path.", e);
        }
    }

    public boolean copySpringBootUberJar(File artifact) throws BoostException {
        try {
            File springJar = new File(getSpringBootUberJarPath(artifact));
            
            // We are sure the artifact is a Spring Boot uber JAR if it has Spring-Boot-Version in the manifest, but not a wlp directory
            if(isSpringBootUberJar(artifact) && !BoostUtil.isLibertyJar(artifact)) {
                FileUtils.copyFile(artifact, springJar);
                return true;
            }
        } catch (IOException e) {
            throw new BoostException("Error copying Spring Boot uber JAR.", e);
        }
        return false;
    }
    
    public void addSpringBootVersionToManifest(File artifact) throws BoostException {
        Path path = artifact.toPath();

       try (FileSystem zipfs = FileSystems.newFileSystem(path, getClass().getClassLoader())) {
            Path zipPath = zipfs.getPath("/META-INF/MANIFEST.MF");
            
            InputStream is = Files.newInputStream(zipPath);
            
            Manifest manifest = new Manifest(is);
            manifest.getMainAttributes().put(new Attributes.Name(BOOT_VERSION_ATTRIBUTE), "1"); // Just put something here
            
            ByteArrayOutputStream manifestOs = new ByteArrayOutputStream();
            manifest.write(manifestOs);
            InputStream manifestIs = new ByteArrayInputStream(manifestOs.toByteArray()); 
            Files.copy(manifestIs, zipPath, StandardCopyOption.REPLACE_EXISTING);
            
        } catch(IOException e) {
            throw new BoostException("Error updating manifest file.", e);
        }
    }
    
}
