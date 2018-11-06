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

package io.openliberty.boost.utils

import io.openliberty.boost.BoostLoggerI

import org.gradle.api.DefaultTask

public class BoostLogger extends DefaultTask implements BoostLoggerI {

    private static BoostLogger logger = null;

    public static BoostLogger getInstance() {
        if (logger == null) {
            logger = new BoostLogger()
        }
        return logger
    }

    @Override
    public void debug(String msg) {
        project.getLogger().debug(msg)
    }

    @Override
    public void debug(String msg, Throwable e) {
        project.getLogger().debug(msg, e)
    }

    @Override
    public void debug(Throwable e) {
        project.getLogger().debug(e)
    }

    @Override
    public void warn(String msg) {
        project.getLogger().warn(msg)
    }

    @Override
    public void info(String msg) {
        project.getLogger().info(msg)
    }

    @Override
    public void error(String msg) {
        project.getLogger().error(msg)
    }

    @Override
    public boolean isDebugEnabled() {
        return project.getLogger().isEnabled(LogLevel.DEBUG)
    }

}