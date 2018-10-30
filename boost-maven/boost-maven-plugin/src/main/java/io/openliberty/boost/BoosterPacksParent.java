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
package io.openliberty.boost;

import java.util.ArrayList;
import java.util.List;

import io.openliberty.boost.BoosterPackConfigurator;

public class BoosterPacksParent {

    /**
     * Creates a list of config writer objects for all boost dependencies found
     */

    List<String> featureList;

    JDBCBoosterPackConfigurator jdbcConfig = null;
    private String JDBC_BOOSTER_PACK_STRING = "liberty-booster-data-jdbc";
    public static String JAXRS_BOOSTER_PACK_STRING_10 = "io.openliberty.boosters:jaxrs:0.1-SNAPSHOT";
    public static String JAXRS_BOOSTER_PACK_STRING_20 = "io.openliberty.boosters:jaxrs:0.2-SNAPSHOT";
    private List<BoosterPackConfigurator> boosterPackConfigList = new ArrayList<BoosterPackConfigurator>();

    /**
     * take a list of pom boost dependency strings and map to liberty features for
     * config. return a list of feature configuration objects for each found
     * dependency.
     * 
     * @param dependencies
     * @return
     */
    public List<BoosterPackConfigurator> mapDependenciesToFeatureList(List<String> dependencies) {

        featureList = new ArrayList<String>();
        for (String dep : dependencies) {
            if (dep.equals(JDBC_BOOSTER_PACK_STRING)) {
                boosterPackConfigList.add(new JDBCBoosterPackConfigurator());
            } else if (dep.startsWith("io.openliberty.boosters:jaxrs:")) {		
                JAXRSBoosterPackConfigurator jaxrsConfig = new JAXRSBoosterPackConfigurator();
                jaxrsConfig.setFeatureString(dep);
                boosterPackConfigList.add(jaxrsConfig);
            }

        }

        return boosterPackConfigList;
    }

    public void writeConfigForFeature(String feature) {

    }
}
