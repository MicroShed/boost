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
package boost.common.runtimes;

import java.util.List;

import boost.common.BoostException;
import boost.common.boosters.AbstractBoosterConfig;

public abstract interface RuntimeI {
    //Will need to pass in plugin mojo/task to Maven/Gradle along with project object

    public void doPackage(List<AbstractBoosterConfig> boosterConfigs, Object project, Object pluginTask) throws BoostException;
    
    public void doDebug(Object project, Object pluginTask) throws BoostException;
    
    public void doRun(Object project, Object pluginTask) throws BoostException;
    
    public void doStart(Object project, Object pluginTask) throws BoostException;
    
    public void doStop(Object project, Object pluginTask) throws BoostException;

}
