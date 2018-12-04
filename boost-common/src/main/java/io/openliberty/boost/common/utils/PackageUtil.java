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

package io.openliberty.boost.common.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import io.openliberty.boost.common.BoostException;
import io.openliberty.boost.common.utils.LibertyServerConfigGenerator;
import io.openliberty.boost.common.boosters.BoosterPackConfigurator;
import io.openliberty.boost.common.boosters.BoosterPacksParent;

public class PackageUtil {
     public static void generateServerXMLJ2EE(List<String> boosterDependencies, String libertyServerPath, String artifactId, String version, String packaging) throws BoostException {
        try {
            LibertyServerConfigGenerator serverConfig = new LibertyServerConfigGenerator(libertyServerPath);

            List<BoosterPackConfigurator> boosterConfigurators = BoosterPacksParent.mapDependenciesToFeatureList(boosterDependencies);

            // Add any other Liberty features needed depending on the boost
            // boosters defined
            List<String> boosterFeatureNames = getBoosterFeatureNames(boosterConfigurators);
            serverConfig.addFeatures(boosterFeatureNames);
            if (packaging.equals("war")) {
                // write out config on behalf of a web app
                serverConfig.addConfigForApp(artifactId, version);
            } else { // only support war packaging type currently
                throw new BoostException(
                        "Unsupported packaging type - Liberty Boost currently supports WAR packaging type only.");
            }
            serverConfig.addConfigForFeatures(boosterConfigurators);

            // Write server.xml to Liberty server config directory
            serverConfig.writeToServer();

        } catch (TransformerException | IOException | ParserConfigurationException e) {
            throw new BoostException("Unable to generate server configuration for the Liberty server.", e);
        }
    }

    private static List<String> getBoosterFeatureNames(List<BoosterPackConfigurator> boosterConfigurators) {
        List<String> featureStrings = new ArrayList<String>();
        for (BoosterPackConfigurator bpconfig : boosterConfigurators) {
            featureStrings.add(bpconfig.getFeatureString());
        }

        return featureStrings;
    }
 }