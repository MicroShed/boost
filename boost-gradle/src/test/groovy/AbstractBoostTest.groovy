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
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Test

import org.apache.commons.io.FileUtils

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.Collections
import java.util.List

public class AbstractBoostTest {

    static File integTestDir = new File('build/testBuilds')
    File testProjectDir

    protected static void deleteDir(File dir) {
        if (dir.exists()) {
            if (!dir.deleteDir()) {
                throw new AssertionError("Unable to delete directory '$dir.canonicalPath'.")
            }
        }
    }
    
    protected static void createDir(File dir) {
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new AssertionError("Unable to create directory '$dir.canonicalPath'.")
            }
        }
    }
    
    protected static File copyBuildFiles(File buildFilename, File buildDir) {
        copyFile(buildFilename, new File(buildDir, 'build.gradle'))
        copyFile(new File("build/gradle.properties"), new File(buildDir, 'gradle.properties'))
    }

    protected static File createTestProject(File parent, File sourceDir, String buildFilename) {
        if (!sourceDir.exists()){
            throw new AssertionError("The source file '${sourceDir.canonicalPath}' doesn't exist.")
        }
        try {
            // Copy all resources except the individual test .gradle files
            // Do copy settings.gradle.
            FileUtils.copyDirectory(sourceDir, parent, new FileFilter() {
               public boolean accept (File pathname) {
                   return (!pathname.getPath().endsWith(".gradle") ||
                    pathname.getPath().endsWith("settings.gradle") ||
                        pathname.getPath().endsWith("build.gradle"))
               }
            })

            // copy the needed gradle build and property files
            File sourceFile = new File(sourceDir, buildFilename)
            copyBuildFiles(sourceFile, parent)

        } catch (IOException e) {
            throw new AssertionError("Unable to copy directory '${parent.canonicalPath}'.")
        }
    }

    protected static File copyFile(File sourceFile, File destFile) {
        if (!sourceFile.exists()){
            throw new AssertionError("The source file '${sourceFile.canonicalPath}' doesn't exist.")
        }
        try {
            FileUtils.copyFile(sourceFile, destFile)
        } catch (Exception e) {
            throw new AssertionError("Unable to create file '${destFile.canonicalPath}'.")
        }
    }

    protected void writeFile(File destination, String content) throws IOException {
        BufferedWriter output = null
        try {
            output = new BufferedWriter(new FileWriter(destination))
            output.write(content)
        } finally {
            if (output != null) {
                output.close()
            }
        }
    }
}

