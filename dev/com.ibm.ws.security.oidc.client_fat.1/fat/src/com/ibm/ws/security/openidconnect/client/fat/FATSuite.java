/*******************************************************************************
 * Copyright (c) 2013, 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.ws.security.openidconnect.client.fat;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.ibm.ws.security.fat.common.actions.LargeProjectRepeatActions;
import com.ibm.ws.security.fat.common.utils.ldaputils.CommonLocalLDAPServerSuite;
import com.ibm.ws.security.openidconnect.client.fat.IBM.OidcClientBasicTests;
import com.ibm.ws.security.openidconnect.client.fat.IBM.OidcClientConsentTests;
import com.ibm.ws.security.openidconnect.client.fat.IBM.OidcClientCookieVerificationTests;
import com.ibm.ws.security.openidconnect.client.fat.IBM.OidcClientDiscoveryBasicTests;
import com.ibm.ws.security.openidconnect.client.fat.IBM.OidcClientDiscoveryErrorBadOpTrustStoreTests;
import com.ibm.ws.security.openidconnect.client.fat.IBM.OidcClientDiscoveryErrorBadRpTrustStoreTests;
import com.ibm.ws.security.openidconnect.client.fat.IBM.OidcClientDiscoveryErrorNoOpTrustStoreTests;
import com.ibm.ws.security.openidconnect.client.fat.IBM.OidcClientDiscoveryErrorNoRpTrustStoreTests;
import com.ibm.ws.security.openidconnect.client.fat.IBM.OidcClientDiscoveryErrorTests;
import com.ibm.ws.security.openidconnect.client.fat.IBM.OidcClientDiscoveryJVMPropsTests;
import com.ibm.ws.security.openidconnect.client.fat.IBM.OidcClientDiscoveryJWTBasicTests;

import componenttest.custom.junit.runner.AlwaysPassesTest;
import componenttest.rules.repeater.RepeatTests;

@RunWith(Suite.class)
@SuiteClasses({
        AlwaysPassesTest.class,
        OidcClientBasicTests.class,
        // OidcClientLogoutTests.class
        OidcClientConsentTests.class,
        OidcClientDiscoveryBasicTests.class,
        OidcClientDiscoveryErrorTests.class,
        OidcClientDiscoveryErrorBadOpTrustStoreTests.class,
        OidcClientDiscoveryErrorBadRpTrustStoreTests.class,
        OidcClientDiscoveryErrorNoOpTrustStoreTests.class,
        OidcClientDiscoveryErrorNoRpTrustStoreTests.class,
        OidcClientDiscoveryJVMPropsTests.class,
        OidcClientDiscoveryJWTBasicTests.class,
        OidcClientCookieVerificationTests.class,
// OidcCertificationRPBasicProfileTests.class,

})
/**
 * Purpose: This suite collects and runs all known good test suites.
 */
public class FATSuite extends CommonLocalLDAPServerSuite {
    /*
     * On Windows, always run the default/empty/EE7/EE8 tests.
     * On other Platforms:
     * - if Java 8, run default/empty/EE7/EE8 tests.
     * - All other Java versions
     * -- If LITE mode, run EE9
     * -- If FULL mode, run EE10
     *
     */
    @ClassRule
    public static RepeatTests repeat = LargeProjectRepeatActions.createEE9OrEE10Repeats();

}
