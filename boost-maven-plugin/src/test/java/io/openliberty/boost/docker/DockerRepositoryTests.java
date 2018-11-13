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
package io.openliberty.boost.docker;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import io.openliberty.boost.maven.docker.DockerBuildMojo;

public class DockerRepositoryTests {

    DockerBuildMojo mojo;

    @Before
    public void setup() {
        mojo = new DockerBuildMojo();
    }

    @Test
    public void testValidRepositoryNames() throws Exception {
        List<String> repositories = Arrays.asList("image", "username/image", "host.com/image",
                "host.com:8080/namespace/image", "host.domain.com/image", "host-name.com/image", "Host.com/image",
                "xn--wkd-8cdx9d7hbd.org/image", "user__name/image__name", "host---name.com/image---name",
                "host.com:8080/namespace__name/image.module-name", "user-name/image-name");

        for (String repository : repositories) {
            assertEquals(repository + " should be valid", true, mojo.isRepositoryValid(repository));
        }
    }

    @Test
    public void testInvalidRepositoryNames() throws Exception {
        List<String> repositories = Arrays.asList("Image", "host_name.com:8080/image", "host__name.com:8080/image",
                "host.com/namepsace-/image", "host__name:8080/image", "host..com:8080/image", "host$name.com/image",
                "user**name/image", "image:8080", "host.com:abcd/image", "-host.com/-image", "user/repo/image/");

        for (String repository : repositories) {
            assertEquals(repository + " should be invalid", false, mojo.isRepositoryValid(repository));
        }
    }

}
