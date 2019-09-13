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
package boost.common.config;

import java.util.Map;
import java.util.Properties;

public class BoosterConfigParams {

    Map<String, String> projectDependencies;
    Properties boostProperties;

    public BoosterConfigParams(Map<String, String> projectDependencies, Properties boostProperties) {
        this.projectDependencies = projectDependencies;
        this.boostProperties = boostProperties;
    }

    public Map<String, String> getProjectDependencies() {
        return this.projectDependencies;
    }

    public Properties getBoostProperties() {
        return this.boostProperties;
    }
}
