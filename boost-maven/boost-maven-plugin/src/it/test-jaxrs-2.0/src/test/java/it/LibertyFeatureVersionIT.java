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
import java.util.Arrays;
import java.io.FileInputStream;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;

public class LibertyFeatureVersionIT {

    private static final String JAXRS_20_FEATURE = "<feature>jaxrs-2.0</feature>";
    private static String SERVER_XML = "target/liberty/wlp/usr/servers/defaultServer/server.xml";
    private static String LIBERTY_PROPERTIES = "target/liberty/wlp/lib/versions/openliberty.properties";
    private static String LIBERTY_VERSION = "com.ibm.websphere.productVersion";
    private static String runtimeVersion;

    @BeforeClass
    public static void init() {
        String runtime = System.getProperty("boostRuntime");
        org.junit.Assume.assumeTrue("ol".equals(runtime) || "wlp".equals(runtime));
        runtimeVersion = System.getProperty("libertyRuntimeVersion");
    }

    /*
     * Go to Maven and figure out what the latest version of liberty runtime is out
     * there. Use that version for comparing what level should be found in the
     * messages.log file when the server is run. Only used when no libertyRunTime
     * version is provided.
     */
    private String getLatesLibertyRuntimeVersion() {
        String libertyVersion = null;

        RemoteRepository central = new RemoteRepository.Builder("central", "default", "http://repo1.maven.org/maven2/")
                .build();
        RepositorySystem repoSystem = newRepositorySystem();
        RepositorySystemSession session = newSession(repoSystem);
        String version = "[19.0.0.6,)";
        Artifact artifact = new DefaultArtifact("io.openliberty:openliberty-runtime:" + version);
        VersionRangeRequest request = new VersionRangeRequest(artifact, Arrays.asList(central), null);
        try {
            VersionRangeResult versionResult = repoSystem.resolveVersionRange(session, request);
            libertyVersion = versionResult.getHighestVersion().toString();
        } catch (VersionRangeResolutionException e) {
            e.printStackTrace();
        }

        return libertyVersion;

    }

    private RepositorySystem newRepositorySystem() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        return locator.getService(RepositorySystem.class);
    }

    private RepositorySystemSession newSession(RepositorySystem system) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        LocalRepository localRepo = new LocalRepository("target/local-repo");
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        return session;
    }

    @Test
    public void testLibertyFeatureVersion() throws Exception {
        File targetFile = new File(SERVER_XML);
        assertTrue(targetFile.getCanonicalFile() + "does not exist.", targetFile.exists());

        // Check contents of file for jaxrs feature
        boolean found = false;
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(SERVER_XML));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(JAXRS_20_FEATURE)) {
                    found = true;
                    break;
                }
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }

        assertTrue("The " + JAXRS_20_FEATURE + " feature was not found in the server configuration", found);
    }

    @Test
    public void testLibertyRuntimeVersion() throws Exception {

        boolean found = false;

        if (runtimeVersion == null) {
            // This relies on a scan of the maven repository to find the lastest version of
            // liberty installed. Not sure what subtle issues this might cause.
            // if there are issues matching the runtime versions then
            // this may be the culprit and not actually a Boost issue.
            runtimeVersion = getLatesLibertyRuntimeVersion();
        }
        String assertMsg = "Runtime version is null. This may be an issue with checking for the latest runtime version from maven and may not be a boost issue";
        assertNotNull(assertMsg, runtimeVersion);

        Properties libertyProps = new Properties();
        String propsRuntime = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(LIBERTY_PROPERTIES);
            libertyProps.load(fis);
            propsRuntime = libertyProps.getProperty(LIBERTY_VERSION);
            assertNotNull("Property runtime empty", propsRuntime);
            if (propsRuntime.equals(runtimeVersion)) {
                found = true;
            }
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        assertMsg = "The run time version  " + runtimeVersion
                + "  was not found in the openliberty.properties. openliberty.properties version = " + propsRuntime
                + ". This may be an issue with checking for the latest runtime version from maven and may not be a boost issue";

        assertTrue(assertMsg, found);
    }
}
