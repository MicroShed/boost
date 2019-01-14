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
package io.openliberty.boost.common.config;

import org.w3c.dom.Document;
import io.openliberty.boost.common.config.BoosterPackConfigurator;
import static io.openliberty.boost.common.config.ConfigConstants.*;

import java.util.Properties;

public class JAXRSBoosterPackConfigurator extends BoosterPackConfigurator {

    String libertyFeature = null;

    public JAXRSBoosterPackConfigurator(String version) {
    	if (version.equals(EE_7_VERSION)) {
            libertyFeature = JAXRS_20;
        } else if (version.equals(EE_8_VERSION)) {
            libertyFeature = JAXRS_21;
        }
    	
    }

    @Override
    public String getFeature() {
        return libertyFeature;
    }

    @Override
    public void addServerConfig(Document doc) {
        // No config to write
    }

	@Override
	public String getDependency() {
		
		return null;
	}

	@Override
	public Properties getServerProperties() {
		
		return null;
	}
}
