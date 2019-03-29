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

package io.openliberty.boost.common.boosters.wildfly;

import java.util.List;

import io.openliberty.boost.common.boosters.AbstractBoosterConfig;

/**
 * Interface to describe common function across all technology Booster Pack
 * Config Objects
 *
 */
public abstract class AbstractBoosterWildflyConfig extends AbstractBoosterConfig {

	/**
	 * Return the command to pass to the wildfly CLI
	 * 
	 * @return
	 */
    public abstract List<String> getCliCommands();

}
