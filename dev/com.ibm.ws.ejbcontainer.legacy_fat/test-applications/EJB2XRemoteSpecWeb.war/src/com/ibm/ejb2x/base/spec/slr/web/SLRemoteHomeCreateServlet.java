/*******************************************************************************
 * Copyright (c) 2002, 2021 IBM Corporation and others.
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
package com.ibm.ejb2x.base.spec.slr.web;

import static org.junit.Assert.assertNotNull;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.naming.NamingException;
import javax.servlet.annotation.WebServlet;

import org.junit.Test;

import com.ibm.ejb2x.base.spec.slr.ejb.SLRa;
import com.ibm.ejb2x.base.spec.slr.ejb.SLRaHome;
import com.ibm.websphere.ejbcontainer.test.tools.FATHelper;

import componenttest.app.FATServlet;

/**
 * <dl>
 * <dt>Test Name:
 * <dd>SLRemoteHomeCreateTest (formerly WSTestSLR_HCTest)
 *
 * <dt>Test Descriptions:
 * <dd>EJB Container basic function tests:
 * <ul>
 * <li>H____ - Home Interface / EJBHome / EJBLocalHome;
 * <li>HCS__ - Home create( short-arg );
 * <li>HCL__ - Home create( long-arg ).
 * </ul>
 *
 * <dt>Command options:
 * <dd>
 * <TABLE width="100%">
 * <COL span="1" width="25%" align="left"> <COL span="1" align="left">
 * <TBODY>
 * <TR> <TH>Option</TH> <TH>Description</TH> </TR>
 * <TR> <TD>None</TD>
 * <TD></TD>
 * </TR>
 * </TBODY>
 * </TABLE>
 *
 * <dt>Test Matrix:
 * <dd>
 * <br>Sub-tests
 * <ul>
 * <li>hcs01 - testHomeCreate - create()
 * <li>hcs02 - testHomeCreateWithKey - create( pk )
 * <li>hcs03 - testHomeCreateWithDuplicateKey - create( pk ) - Duplicate key
 * <li>hcl01 - testHomeCreateWithMultiArgs - create( long-args )
 * <li>hcl02 - testHomeCreateWithDuplicateMultiArgs - create( long-args ) - Duplicate key
 * </ul>
 * <br>Data Sources
 * </dl>
 */
@SuppressWarnings("serial")
@WebServlet("/SLRemoteHomeCreateServlet")
public class SLRemoteHomeCreateServlet extends FATServlet {
    private final static String CLASS_NAME = SLRemoteHomeCreateServlet.class.getName();
    private final static Logger svLogger = Logger.getLogger(CLASS_NAME);

    private final static String ejbJndiName1 = "com/ibm/ejb2x/base/spec/slr/ejb/SLRaBMTHome";
    private static SLRaHome fhome1;

    @PostConstruct
    private void initializeHomes() {
        try {
            fhome1 = FATHelper.lookupRemoteHomeBinding(ejbJndiName1, SLRaHome.class);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * (hcs01) Test Stateless remote home create (no parameters)
     */
    @Test
    public void testSLRemoteHomeCreate() throws Exception {
        SLRa ejb1 = fhome1.create();
        assertNotNull("Create EJB was null.", ejb1);
        ejb1.remove();
    }

    /**
     * (hcs02) Test Stateless remote home create (with primary key)
     */
    //@Test
    public void testSLRemoteHomeCreateWithKey() {
        svLogger.info("This test does not apply to Stateless beans.");
    }

    /**
     * (hcs03) Test Stateless remote home create (with duplicate primary key)
     */
    //@Test
    public void testSLRemoteHomeCreateWithDuplicateKey() {
        svLogger.info("This test does not apply to Stateless beans.");
    }

    /**
     * (hcl01) Test Stateless remote home create (with multiple args)
     */
    //@Test
    public void testSLRemoteHomeCreateWithMultiArgs() {
        svLogger.info("This test does not apply to Stateless beans.");
    }

    /**
     * (hcl02) Test Stateless remote home create (with duplicate multiple args)
     */
    //@Test
    public void testSLRemoteHomeCreateWithDuplicateMultiArgs() {
        svLogger.info("This test does not apply to Stateless beans.");
    }
}