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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.command.PullImageResultCallback;

public class DockerPushIT {
    
    private static DockerClient dockerClient;
    
    @BeforeClass
    public static void setup() throws Exception {
        dockerClient = DockerClientBuilder.getInstance().build();
    } 
   
    
    @Test
    public void testPushDockerImageToLocalRegistry() throws Exception {        
        long sizeOfPushedImage = getSizeOfImage();
        String idOfPushedImage = getId();
        
        //Remove the local image.
        dockerClient.removeImageCmd("localhost:5000/test-docker:latest").exec();
        Thread.sleep(2000);
        
        //Pull the image from the local repository which got pushed by the plugin. This is possible if the plugin successfully pushed to the registry.
        dockerClient.pullImageCmd("localhost:5000/test-docker").withTag("latest").exec(new PullImageResultCallback()).awaitCompletion(10, TimeUnit.SECONDS);
        
        long sizeOfPulledImage = getSizeOfImage();
        String idOfPulledImage = getId();
       
        assertEquals("Expected image was not pulled, size doesn't match.", sizeOfPushedImage, sizeOfPulledImage);
        assertEquals("Expected image was not pulled, id doesn't match.", idOfPushedImage, idOfPulledImage);
    }
    
    private long getSizeOfImage() {
        List<Image> images = dockerClient.listImagesCmd().exec();
        return images.get(0).getSize();
    }
    
    private String getId() {
        List<Image> images = dockerClient.listImagesCmd().exec();
        return images.get(0).getId();
    }
    
    
}