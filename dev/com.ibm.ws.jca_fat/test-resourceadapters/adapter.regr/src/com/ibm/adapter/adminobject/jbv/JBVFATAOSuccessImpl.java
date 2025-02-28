/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.adapter.adminobject.jbv;

import javax.validation.constraints.Min;

/**
 * This class is an Administered Object JavaBean that is used for testing
 * Java Bean Validation. It is annotated with constraints and also extends a
 * class which is annotated with more constraints.
 */
public class JBVFATAOSuccessImpl extends JBVFATAOImpl {

    @Min(value = 10)
    Float aoProperty4;

    public Float getAoProperty4() {
        return aoProperty4;
    }

    public void setAoProperty4(Float aoProperty4) {
        this.aoProperty4 = aoProperty4;
    }

}
