/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package io.openliberty.boost.runtimes.utils;

import boost.common.BoostLoggerI;

public class CommonLogger implements BoostLoggerI {
    
    private static CommonLogger logger = null;

    public static CommonLogger getInstance() {
        if (logger == null) {
            logger = new CommonLogger();
        }
        return logger;
    }

    @Override
    public void debug(String msg) {
        System.out.println("debug: " + msg);
    }

    @Override
    public void debug(String msg, Throwable e) {
        debug(msg);
        debug(e);
    }

    @Override
    public void debug(Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void warn(String msg) {
        System.out.println("warn: " + msg);
    }

    @Override
    public void info(String msg) {
        System.out.println("info: " + msg);
    }

    @Override
    public void error(String msg) {
        System.out.println("error: " + msg);
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

}