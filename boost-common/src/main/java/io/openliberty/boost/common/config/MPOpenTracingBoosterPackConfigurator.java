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

import static io.openliberty.boost.common.config.ConfigConstants.MPOPENTRACING_10;

import org.w3c.dom.Document;

public class MPOpenTracingBoosterPackConfigurator extends BoosterPackConfigurator {

    public MPOpenTracingBoosterPackConfigurator(BoosterDependencyInfo depInfo, LibertyServerConfigGenerator srvrXML) {
		super(depInfo, srvrXML);
		// TODO Auto-generated constructor stub
	}

	public void addServerConfig(Document doc) {
		// write out the feature Manager stanza
		if (dependencyInfo.getVersion().equals(MP_20_VERSION)) {
			serverXML.addFeature(MPOPENTRACING_10);
		}
	}
}
