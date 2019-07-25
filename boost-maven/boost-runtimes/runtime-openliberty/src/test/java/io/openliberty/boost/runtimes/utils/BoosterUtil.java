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

import java.util.HashMap;
import java.util.Map;

import boost.common.BoostException;
import boost.common.boosters.AbstractBoosterConfig;
import boost.common.boosters.JDBCBoosterConfig;

public class BoosterUtil {

    public static Map<String, String> createDependenciesWithBoosterAndVersion(Class<?> booster, String version)
            throws BoostException {
        Map<String, String> map = new HashMap<String, String>();
        map.put(AbstractBoosterConfig.getCoordinates(booster), version);
        return map;
    }

    public static Map<String, String> getJDBCDependency() throws BoostException {
        return BoosterUtil.createDependenciesWithBoosterAndVersion(JDBCBoosterConfig.class, "0.1-SNAPSHOT");
    }
}