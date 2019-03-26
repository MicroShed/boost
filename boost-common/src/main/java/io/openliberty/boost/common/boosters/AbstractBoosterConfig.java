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

package io.openliberty.boost.common.boosters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.Document;

import io.openliberty.boost.common.BoostException;

/**
 * Interface to describe common function across all technology Booster Pack
 * Config Objects
 *
 */
public abstract class AbstractBoosterConfig {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface BoosterCoordinates {
        String value();
    }

    public static String getCoordindates(Class<?> klass) throws BoostException {
        BoosterCoordinates coordinates = klass.getAnnotation(BoosterCoordinates.class);
        if (coordinates == null) {
            throw new BoostException(
                    String.format("class '%s' must have a BoosterCoordinates annotation", klass.getName()));
        }
        return coordinates.value();
    }

    protected static final String BOOSTERS_GROUP_ID = "io.openliberty.boosters";
    protected String EE_7_VERSION = "0.1-SNAPSHOT";
    protected String EE_8_VERSION = "0.2-SNAPSHOT";
    protected String MP_20_VERSION = "0.2-SNAPSHOT";

    /**
     * Return the Liberty feature name
     * 
     * @return
     */
    public abstract String getFeature();

    /**
     * Add the server.xml configuration for this booster
     * 
     * @param doc
     */
    public abstract void addServerConfig(Document doc);

    /**
     * Return the properties required by this booster
     */
    public abstract Properties getServerProperties();

    /**
     * Return the dependency that this booster requires
     * 
     * @return
     */
    public abstract String getDependency();

    public abstract List<String> getTomEEDependency();

}
