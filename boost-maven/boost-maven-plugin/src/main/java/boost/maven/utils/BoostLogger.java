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
package boost.maven.utils;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.codehaus.mojo.pluginsupport.MojoSupport;

import boost.common.BoostLoggerI;

public class BoostLogger implements BoostLoggerI {

    private org.apache.maven.plugin.logging.Log log;
    
    public org.apache.maven.plugin.logging.Log getLog() {
        return log;
    }
    
    public BoostLogger(org.apache.maven.plugin.logging.Log log) {
        this.log = log;
    }

    public static BoostLogger getSystemStreamLogger() {
        return new BoostLogger(new SystemStreamLog());
    }

    @Override
    public void debug(String msg) {
        getLog().debug(msg);
    }

    @Override
    public void debug(String msg, Throwable e) {
        getLog().debug(msg, e);
    }

    @Override
    public void debug(Throwable e) {
        getLog().debug(e);
    }

    @Override
    public void warn(String msg) {
        getLog().warn(msg);
    }

    @Override
    public void info(String msg) {
        getLog().info(msg);
    }

    @Override
    public void error(String msg) {
        getLog().error(msg);
    }

    @Override
    public boolean isDebugEnabled() {
        return getLog().isDebugEnabled();
    }

}
