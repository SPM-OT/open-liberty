/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
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
/*
 * Some of the code was derived from code supplied by the Apache Software Foundation licensed under the Apache License, Version 2.0.
 */
package com.ibm.ws.transport.iiop.security;

import org.omg.CORBA.NO_PERMISSION;

/**
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public class SASInvalidEvidenceException extends SASException {

    public SASInvalidEvidenceException() {
        super(1, new NO_PERMISSION());
    }

    /**
     * @param message the message used in the creation of the NO_PERMISSION exception.
     * @param noPermissionMinorCode the minor code used in the creation of the NO_PERMISSION exception.
     */
    public SASInvalidEvidenceException(String message, int noPermissionMinorCode) {
        super(1, new org.omg.CORBA.NO_PERMISSION(message,
                        noPermissionMinorCode,
                        org.omg.CORBA.CompletionStatus.COMPLETED_NO));
    }

}
