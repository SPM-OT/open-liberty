/*******************************************************************************
 * Copyright (c) 2023 IBM Corporation and others.
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

package io.openliberty.checkpoint.fat;

import static io.openliberty.checkpoint.fat.FATSuite.stopServer;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

import com.ibm.websphere.simplicity.ShrinkHelper;
import com.ibm.ws.transaction.fat.util.FATUtils;

import componenttest.annotation.Server;
import componenttest.annotation.SkipIfCheckpointNotSupported;
import componenttest.annotation.TestServlet;
import componenttest.custom.junit.runner.FATRunner;
import componenttest.rules.repeater.JakartaEE10Action;
import componenttest.rules.repeater.JakartaEE9Action;
import componenttest.rules.repeater.RepeatTests;
import componenttest.topology.impl.LibertyServer;
import componenttest.topology.utils.FATServletClient;
import io.openliberty.checkpoint.spi.CheckpointPhase;
import servlets.simple.SimpleServlet;

/**
 * Verify servlets have JNDI access to transaction mgmt APIs and can perform basic
 * transaction operations upon restoring a server checkpointed at=applications.
 *
 * The test also verifies changes to the datasource configuration are picked up at
 * restore time before the datasource is injected into the test servlet.
 *
 * Essentially, this is a bringup test for transaction management services and the
 * JTA provider for checkpoint and restore.
 */
@RunWith(FATRunner.class)
@SkipIfCheckpointNotSupported
public class ServletTest extends FATServletClient {

    static final String SERVER_NAME = "checkpointTransactionServlet";

    @ClassRule
    public static RepeatTests r = RepeatTests.withoutModification()
                    .andWith(new JakartaEE9Action().forServers(SERVER_NAME).fullFATOnly())
                    .andWith(new JakartaEE10Action().forServers(SERVER_NAME).fullFATOnly());

    static final String APP_NAME = "transactionservlet";
    static final String SERVLET_NAME = APP_NAME + "/SimpleServlet";

    @Server(SERVER_NAME)
    @TestServlet(servlet = SimpleServlet.class, contextRoot = APP_NAME)
    public static LibertyServer server;

    private static String DERBY_DS_JNDINAME = "jdbc/derby"; // Differs from server.xml config

    @BeforeClass
    public static void setUpClass() throws Exception {
        ShrinkHelper.defaultApp(server, APP_NAME, "servlets.simple.*");

        Consumer<LibertyServer> preRestoreLogic = checkpointServer -> {
            // Configure the datasource jndiName used by the test servlet upon restore
            File serverEnvFile = new File(checkpointServer.getServerRoot() + "/server.env");
            try (PrintWriter serverEnvWriter = new PrintWriter(new FileOutputStream(serverEnvFile))) {
                serverEnvWriter.println("DERBY_DS_JNDINAME=" + DERBY_DS_JNDINAME);
            } catch (FileNotFoundException e) {
                throw new UncheckedIOException(e);
            }
            // Verify the application starts during checkpoint
            assertNotNull("'SRVE0169I: Loading Web Module: " + APP_NAME + "' message not found in log before rerstore",
                          server.waitForStringInLogUsingMark("SRVE0169I: .*" + APP_NAME, 0));
            assertNotNull("'CWWKZ0001I: Application " + APP_NAME + " started' message not found in log.",
                          server.waitForStringInLogUsingMark("CWWKZ0001I: .*" + APP_NAME, 0));
        };
        server.setCheckpoint(CheckpointPhase.APPLICATIONS, false, preRestoreLogic);
        server.setServerStartTimeout(FATUtils.LOG_SEARCH_TIMEOUT);
        server.startServer();
        server.checkpointRestore();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        stopServer(server, "WTRN0017W"); // Unable to begin nested tran; nested trans not supported
        ShrinkHelper.cleanAllExportedArchives();
    }

}
