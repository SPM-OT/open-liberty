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
package io.openliberty.checkpoint.fat.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.time.Duration;
import java.time.Instant;

import com.ibm.websphere.simplicity.ProgramOutput;
import com.ibm.websphere.simplicity.log.Log;

import componenttest.topology.impl.LibertyServer;
import componenttest.topology.utils.FATServletClient;

public class RecoveryUtils {

    private static final Class<RecoveryUtils> c = RecoveryUtils.class;

    public static final Duration TESTCONTAINER_STARTUP_TIMEOUT = Duration.ofMinutes(5);
    public static final int LOG_SEARCH_TIMEOUT = 300000;

    private static final int RETRY_COUNT = 5;
    private static final int RETRY_INTERVAL = 10000;

    private static final FATServletClient fsc = new FATServletClient();

    public static void recoveryTest(LibertyServer server, String servletName, String id) throws Exception {
        recoveryTest(server, server, servletName, id);
    }

    public static void recoveryTest(LibertyServer crashingServer, LibertyServer recoveringServer, String servletName, String id) throws Exception {
        final String method = "recoveryTest";

        try {
            // We expect this to fail since it is gonna crash the server
            FATServletClient.runTest(crashingServer, servletName, "setupRec" + id);
            restartServer(crashingServer);
            fail(crashingServer.getServerName() + " did not crash as expected");
        } catch (Exception e) {
            Log.info(RecoveryUtils.class, method, "setupRec" + id + " crashed as expected");
        }

        assertNotNull(crashingServer.getServerName() + " didn't crash properly", crashingServer.waitForStringInLog("Dump State: "));

        crashingServer.postStopServerArchive(); // must explicitly collect since server start failed

        RecoveryUtils.startServers(recoveringServer);

        // Server appears to have started ok
        assertNotNull(recoveringServer.getServerName() + " didn't recover properly",
                      recoveringServer.waitForStringInTrace("Performed recovery for " + crashingServer.getServerName()));

        int attempt = 0;
        while (true) {
            Log.info(RecoveryUtils.class, method, "calling checkRec" + id);
            try {
                final StringBuilder sb = fsc.runTestWithResponse(recoveringServer, servletName, "checkRec" + id);
                Log.info(RecoveryUtils.class, method, "checkRec" + id + " returned: " + sb);
                break;
            } catch (Exception e) {
                Log.error(RecoveryUtils.class, method, e);
                if (++attempt < 5) {
                    Thread.sleep(10000);
                } else {
                    // Something is seriously wrong with this server instance.
                    // Reset so the next test has a chance
                    restartServer(recoveringServer);
                    throw e;
                }
            }
        }
    }

    private static void restartServer(LibertyServer s) {
        try {
            RecoveryUtils.stopServers(new String[] { ".*" }, s);
            RecoveryUtils.startServers(s);
            s.printProcessHoldingPort(s.getHttpDefaultPort());
        } catch (Exception e) {
            Log.error(RecoveryUtils.class, "restartServer", e);
        }
    }

//    public static Duration startServers(LibertyServer... servers) throws Exception {
//        return startServers((SetupRunner) null, servers);
//    }

    /**
     * @param r
     * @param servers
     * @return Mean server start time
     * @throws Exception
     */
    public static Duration startServers(/* SetupRunner r, */ LibertyServer... servers) throws Exception {
        final String method = "startServers";

        Instant startStart = Instant.now();

        for (LibertyServer server : servers) {
            assertNotNull("Attempted to start a null server", server);
            int attempt = 0;
            int maxAttempts = 5;

            Log.info(c, method, "Starting " + server.getServerName());

            do {
                if (attempt++ > 0) {
                    Log.info(c, method, "Waiting 5 seconds after start failure before making attempt " + attempt);
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        Log.error(c, method, e);
                    }
                }

                // This next test appears to be a bit dodgy on z/OS. Since the test clearly
                // thinks the server is stopped, we'll retry hoping it's gone on the next attempt.
                if (server.resetStarted() == 0) {
                    String pid = server.getPid();
                    Log.info(c, method,
                             "Server " + server.getServerName() + " is already running. (pid: " + ((pid != null ? pid : "unknown")) + ")");
                    server.printProcesses();

                    continue;
                }

                ProgramOutput po = null;
                try {
                    //if (r != null) {
                    //    r.run(server);
                    //}
                    Instant serverStart = Instant.now();
                    //po = server.startServerAndValidate(false, false, true);
                    po = server.checkpointRestore();
                    Log.info(c, method, server.getServerName() + " started in " + Duration.between(serverStart, Instant.now()));
                } catch (Exception e) {
                    Log.error(c, method, e, "Server start attempt " + attempt + " failed with return code " + (po != null ? po.getReturnCode() : "<unavailable>"));
                }

                if (po != null) {
                    String s;

                    int rc = po.getReturnCode();
                    Log.info(c, method, "ReturnCode: " + rc);

                    s = server.getPid();
                    if (s != null && !s.isEmpty())
                        Log.info(c, method, "Pid: " + s);

                    s = po.getStdout();
                    if (s != null && !s.isEmpty())
                        Log.info(c, method, "Stdout: " + s.trim());

                    s = po.getStderr();
                    if (s != null && !s.isEmpty())
                        Log.info(c, method, "Stderr: " + s.trim());

                    if (rc == 0) {
                        break;
                    } else {
                        String pid = server.getPid();
                        Log.info(c, method,
                                 "Non zero return code starting server " + server.getServerName() + "." + ((pid != null ? "(pid:" + pid + ")" : ""))
                                            + " Maybe it is on the way down.");
                        server.printProcessHoldingPort(server.getHttpDefaultPort());
                    }
                } else {
                    // Start failed in an unusual way. Make sure it's stopped prior to next attempt.
                    try {
                        server.stopServer(".*");
                    } catch (Exception e) {
                        Log.error(c, method, e);
                    }
                }
            } while (attempt < maxAttempts);

            if (!server.isStarted()) {
                server.postStopServerArchive();
                throw new Exception("Failed to start " + server.getServerName() + " after " + attempt + " attempts");
            }
        }

        return Duration.between(startStart, Instant.now()).dividedBy(servers.length); // better not be 0
    }

    public static void stopServers(String[] toleratedMsgs, LibertyServer... servers) throws Exception {
        final String method = "stopServers";

        for (LibertyServer server : servers) {
            assertNotNull("Attempted to stop a null server", server);
            int attempt = 0;
            int maxAttempts = 5;
            Log.info(c, method, "Stopping " + server.getServerName());
            do {
                if (attempt++ > 0) {
                    Log.info(c, method, "Waiting 5 seconds after stop failure before making attempt " + attempt);
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        Log.error(c, method, e);
                    }
                }

                if (!server.isStarted()) {
                    Log.info(c, method,
                             "Server " + server.getServerName() + " is not started. No need to stop it.");
                    break;
                }

                ProgramOutput po = null;
                try {
                    po = server.stopServer(toleratedMsgs);
                } catch (Exception e) {
                    Log.error(c, method, e, "Server stop attempt " + attempt + " failed with return code " + (po != null ? po.getReturnCode() : "<unavailable>"));
                }

                if (po != null) {
                    String s;

                    int rc = po.getReturnCode();
                    Log.info(c, method, "ReturnCode: " + rc);

                    if (rc == 0) {
                        break;
                    } else {
                        String pid = server.getPid();
                        Log.info(c, method,
                                 "Non zero return code stopping server " + server.getServerName() + "." + ((pid != null ? "(pid:" + pid + ")" : "")));

                        s = po.getStdout();
                        if (s != null && !s.isEmpty())
                            Log.info(c, method, "Stdout: " + s.trim());

                        s = po.getStderr();
                        if (s != null && !s.isEmpty())
                            Log.info(c, method, "Stderr: " + s.trim());

                        server.printProcessHoldingPort(server.getHttpDefaultPort());
                    }
                }
            } while (attempt < maxAttempts);

            if (server.isStarted()) {
                server.postStopServerArchive();
                throw new Exception("Failed to stop " + server.getServerName() + " after " + attempt + " attempts");
            }
        }
    }

    public static void stopServers(LibertyServer... servers) throws Exception {
        stopServers((String[]) null, servers);
    }

    public static <T> T runWithRetries(Repeatable<T> r) throws Exception {
        return runWithRetries(RETRY_COUNT, RETRY_INTERVAL, r);
    }

    public static <T> T runWithRetries(int retryCount, int retryInterval, Repeatable<T> r) throws Exception {
        int attempt = 0;
        while (true) {
            try {
                return r.execute();
            } catch (Exception e) {
                Log.error(RecoveryUtils.class, "runWithRetries", e);
                if (++attempt < retryCount) {
                    Thread.sleep(retryInterval);
                } else {
                    throw e;
                }
            }
        }
    }

    @FunctionalInterface
    public interface Repeatable<T> {
        T execute() throws Exception;
    }

    // A test artifact created by com.ibm.tx.jta.ut.util.XAResourceImpl.java
    public static void deleteXARecoveryDat(LibertyServer ls) throws Exception {
        if (ls.fileExistsInLibertyServerRoot("XARecovery.dat")) {
            ls.deleteFileFromLibertyServerRoot("XARecovery.dat");
        }
    }

}