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
package io.openliberty.boost.common.docker;

import java.text.MessageFormat;

import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ProgressMessage;

import io.openliberty.boost.common.BoostLoggerI;

public class DockerLoggingProgressHandler implements ProgressHandler {

    private final BoostLoggerI log;

    public DockerLoggingProgressHandler(BoostLoggerI log) {
        this.log = log;
    }

    @Override
    public void progress(ProgressMessage message) throws DockerException {
        String stream = message.stream();
        String status = message.status();
        String error = message.error();

        if (error != null) {
            throw new DockerException(error);

        } else if (message.progressDetail() != null) {
            logProgress(message.id(), status);

        } else if (status != null || stream != null) {
            logStream(stream, status);
        }
    }

    private void logProgress(String id, String status) {
        if (id != null) {
            log.info(MessageFormat.format("{0}: {1}", id, status));
        }
    }

    private void logStream(String stream, String status) {
        final String value = (stream != null) ? trimNewline(stream) : status;
        log.info(value);
    }

    private static String trimNewline(String string) {
        return (string != null && string.endsWith("\n")) ? string.substring(0, string.length() - 1) : string;
    }
}
