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

import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.BeforeClass
import org.junit.Test
import java.util.concurrent.TimeUnit

import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.api.model.Image
import com.github.dockerjava.core.command.PullImageResultCallback

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertNotNull

public class DockerPush20Test extends AbstractBoostDockerTest {

    private static String imageName = "localhost:5000/test-image20:latest"

    @BeforeClass
    public static void setup() {

        resourceDir = new File("build/resources/test/springApp")
        testProjectDir = new File(integTestDir, "DockerPush20Test")
        buildFilename = "docker20Test.gradle"
        libertyImage = OL_SPRING_20_IMAGE
        repository = "localhost:5000/test-image20"
        dockerPort = "9080"

        createDir(testProjectDir)
        createTestProject(testProjectDir, resourceDir, buildFilename)
        dockerFile = new File(testProjectDir, "Dockerfile")
        dockerClient = DockerClientBuilder.getInstance().build()

        result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .forwardOutput()
            .withArguments("boostDockerPush", "-i", "-s")
            .build()
    }

    @Test
    public void testPushSuccess() throws Exception {
        assertEquals(SUCCESS, result.task(":boostDockerPush").getOutcome())
    }

    @Test
    public void testPushDockerImageToLocalRegistry() throws Exception {
        Image pushedImage = getImage(imageName)
        assertNotNull(imageName + " was not built.", pushedImage)

        long sizeOfPushedImage = pushedImage.getSize()
        String idOfPushedImage = pushedImage.getId()

        // Remove the local image.
        removeImage(imageName)

        // Pull the image from the local repository which got pushed by the plugin. This
        // is possible if the plugin successfully pushed to the registry.
        dockerClient.pullImageCmd("${repository}").withTag("latest").exec(new PullImageResultCallback())
                .awaitCompletion(10, TimeUnit.SECONDS)

        Image pulledImage = getImage(imageName)
        assertNotNull(repository + " was not pulled.", pulledImage)

        long sizeOfPulledImage = pulledImage.getSize()
        String idOfPulledImage = pulledImage.getId()

        assertEquals("Expected image was not pulled, size doesn't match.", sizeOfPushedImage, sizeOfPulledImage)
        assertEquals("Expected image was not pulled, id doesn't match.", idOfPushedImage, idOfPulledImage)
    }

    private Image getImage(String repository) throws Exception {
        List<Image> images = dockerClient.listImagesCmd().exec()
        for (Image image : images) {
            String[] repoTags = image.getRepoTags()
            if (repoTags != null) {
                String repoTag = repoTags[0]
                if (repoTag != null && repoTag.equals(repository)) {
                    return image
                }
            }
        }
        return null;
    }

    private void removeImage(String repository) throws Exception {
        dockerClient.removeImageCmd(repository).exec()
        Image removedImage = getImage(repository)
        assertNull(repository + " was not removed.", removedImage)
    }
}