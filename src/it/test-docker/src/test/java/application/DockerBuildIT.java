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

package application;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Test;
import org.junit.BeforeClass;
import java.util.List;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.core.DockerClientBuilder;


public class DockerBuildIT {
    
    @Test
    public void testDefaultRepositoryName() throws Exception {
        File repository = new File("target/docker/repository");
        if(repository != null && repository.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(repository));
            String line = reader.readLine();
            assertEquals("Expected default Docker Repository ${project.artifactId}  was not created", "test-docker", line);
        }
    }
    
    @Test
    public void testDefaultTag() throws Exception {
        File tag = new File("target/docker/tag");
        if(tag != null && tag.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(tag));
            String line = reader.readLine();
            assertEquals("Expected default Docker Tag ${project.version} was not created", "1.0.0", line);
        }
    }
    
    @Test
    public void testDefaultImageName() throws Exception {
        File imageName = new File("target/docker/image-name");
        if(imageName != null && imageName.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(imageName));
            String line = reader.readLine();
            assertEquals("Expected default Docker Image ${project.artifactId}:${project.version} was not created", "test-docker:1.0.0", line);
        }
    }
    
    @Test
    public void runDockerContainerAndVerifyAppOnEndpoint() throws Exception {
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        CreateContainerResponse container = dockerClient.createContainerCmd("test-docker:1.0.0").withPortBindings(PortBinding.parse("9082:9080")).exec();
        Thread.sleep(3000);
        
        stopAnyPreviouslyRunningContainers(dockerClient);
        
        dockerClient.startContainerCmd(container.getId()).exec();
        
        Thread.sleep(3000);
        testDockerContainerRunning(dockerClient);
        
        Thread.sleep(10000);
        testAppRunningOnEndpoint();

        dockerClient.killContainerCmd(container.getId()).exec();       
    }
    
    public void stopAnyPreviouslyRunningContainers(DockerClient dockerClient) throws Exception {
        List<Container> containers = dockerClient.listContainersCmd().exec();
        
        for(Container container: containers) {
            dockerClient.stopContainerCmd(container.getId()).exec();
        }
    }
    
    public void testDockerContainerRunning(DockerClient dockerClient) throws Exception{
        List<Container> containers = dockerClient.listContainersCmd().exec();
        assertEquals("Expected number of running containers not found", 1, containers.size());
    }
    
    public void testAppRunningOnEndpoint() throws Exception{
        URL requestUrl = new URL("http://localhost:9082/spring/");
        HttpURLConnection conn = (HttpURLConnection) requestUrl.openConnection();

        if (conn != null) {
            assertEquals("Expected response code not found.", 200, conn.getResponseCode());
        }

        StringBuffer response = new StringBuffer();

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }
        assertEquals("Expected body not found.", "Greetings from Spring Boot!", response.toString());
    }
}