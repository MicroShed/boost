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
package boost.runtimes.openliberty;

public class OpenLibertyRuntime extends LibertyRuntime {

    public OpenLibertyRuntime() {
        super("io.openliberty", "openliberty-runtime", "19.0.0.3");
    }

}
