/*******************************************************************************
 * Copyright (c) 2018, 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.microshed.boost.common.boosters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.microshed.boost.common.BoostException;
import org.microshed.boost.common.BoostLoggerI;
import org.microshed.boost.common.boosters.AbstractBoosterConfig.BoosterCoordinates;
import org.microshed.boost.common.config.BoosterConfigParams;

@BoosterCoordinates(AbstractBoosterConfig.BOOSTERS_GROUP_ID + ":mp-jwt")
public class MPJWTBoosterConfig extends AbstractBoosterConfig {

    protected Properties boostMPProperties;
    // assuming properties will be configured with underscore
    private final static String JWT_ISSUER = "mp_jwt_verify_issuer";
    private final static String JWT_PUBLIC_KEY = "mp_jwt_verify_publickey";
    private final static String JWT_PUBLIC_KEY_LOCATION = "mp_jwt_verify_publickey_location";

    public MPJWTBoosterConfig(BoosterConfigParams params, BoostLoggerI logger) throws BoostException {
        super(params.getProjectDependencies().get(getCoordinates(MPJWTBoosterConfig.class)));

        boostMPProperties = new Properties();
        Properties inputProp = params.getBoostProperties();
        String issuer = inputProp.getProperty(JWT_ISSUER);
        String publicKey = inputProp.getProperty(JWT_PUBLIC_KEY);
        String publicKeyLoc = inputProp.getProperty(JWT_PUBLIC_KEY_LOCATION);
        logger.info("Issuer = " + issuer);
        logger.info("public key = " + publicKey);
        logger.info("public key loc = " + publicKeyLoc);
        if (issuer != null)
            boostMPProperties.setProperty(JWT_ISSUER, issuer);
        if (publicKey != null)
            boostMPProperties.setProperty(JWT_PUBLIC_KEY, publicKey);
        else if (publicKeyLoc != null)
            boostMPProperties.setProperty(JWT_PUBLIC_KEY_LOCATION, publicKeyLoc);

    }

    @Override
    public List<String> getDependencies() {
        return new ArrayList<String>();
    }

}
