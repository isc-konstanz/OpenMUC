/*
 * Copyright 2011-2020 Fraunhofer ISE
 *
 * This file is part of OpenMUC.
 * For more information visit http://www.openmuc.org
 *
 * OpenMUC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenMUC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenMUC.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.openmuc.framework.driver.iec61850;

import static org.hamcrest.CoreMatchers.instanceOf;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.driver.spi.Connection;
import org.openmuc.framework.driver.spi.ConnectionException;

import com.beanit.openiec61850.BasicDataAttribute;
import com.beanit.openiec61850.ClientSap;
import com.beanit.openiec61850.SclParseException;
import com.beanit.openiec61850.SclParser;
import com.beanit.openiec61850.ServerEventListener;
import com.beanit.openiec61850.ServerModel;
import com.beanit.openiec61850.ServerSap;
import com.beanit.openiec61850.ServiceError;

public class Iec61850DriverTest extends Thread implements ServerEventListener {

    int port = 54321;
    String host = "127.0.0.1";
    ClientSap clientSap = new ClientSap();
    ServerSap serverSap = null;
    ServerModel serversServerModel = null;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeEach
    public void initialize() throws SclParseException, IOException {
        new Iec61850Driver();
        clientSap.setTSelRemote(new byte[] { 0, 1 });
        clientSap.setTSelLocal(new byte[] { 0, 0 });

        // ---------------------------------------------------
        // -----------------start test server------------------
        serverSap = runServer("src/test/resources/testOpenmuc.icd", port, serverSap, serversServerModel);
        System.out.println("IED Server is running");
        clientSap.setApTitleCalled(new int[] { 1, 1, 999, 1 });
    }

    @Test
    public void testConnectEmptySettings() throws ArgumentSyntaxException, ConnectionException {
        // test with valid syntax on the test server
        String testDeviceAdress = "127.0.0.1:54321";
        String testSettings = "";
        Iec61850Driver testIec61850Driver = new Iec61850Driver();
        Connection testIec61850Connection = testIec61850Driver.connect(testDeviceAdress, testSettings);
        Assert.assertThat(testIec61850Connection, instanceOf(Connection.class));
        testIec61850Connection.disconnect();
    }

    @Test
    public void testConnectValidSettings1() throws Exception {
        // Test 1
        String testDeviceAdress = "127.0.0.1:54321";
        String testSettings = "-a 12 -lt 1 -rt 1";
        Iec61850Driver testIec61850Driver = new Iec61850Driver();
        Connection testIec61850Connection = testIec61850Driver.connect(testDeviceAdress, testSettings);
        Assert.assertThat(testIec61850Connection, instanceOf(Connection.class));
        testIec61850Connection.disconnect();
    }

    @Test
    public void testConnectValidSettings2() throws Exception {
        // Test 1
        String testDeviceAdress = "127.0.0.1:54321";
        String testSettings = "-a 12 -lt -rt ";
        Iec61850Driver testIec61850Driver = new Iec61850Driver();
        Connection testIec61850Connection = testIec61850Driver.connect(testDeviceAdress, testSettings);
        Assert.assertThat(testIec61850Connection, instanceOf(Connection.class));
        testIec61850Connection.disconnect();
    }

    @Test
    public void testConnectInvalidSettings1() throws Exception {
        // Test 1
        String testDeviceAdress = "127.0.0.1:54321";
        String testSettings = "-a -lt 1 -rt 1";
        Iec61850Driver testIec61850Driver = new Iec61850Driver();
        thrown.expect(ArgumentSyntaxException.class);
        thrown.expectMessage("No authentication parameter was specified after the -a parameter");
        Connection testIec61850Connection = testIec61850Driver.connect(testDeviceAdress, testSettings);
        testIec61850Connection.disconnect();
    }

    @Test
    public void testConnectInvalidSettings2() throws Exception {
        // Test 1
        String testDeviceAdress = "127.0.0.1:54321";
        String testSettings = "-a 12";
        Iec61850Driver testIec61850Driver = new Iec61850Driver();
        thrown.expect(ArgumentSyntaxException.class);
        thrown.expectMessage("Less than 4 or more than 6 arguments in the settings are not allowed.");
        Connection testIec61850Connection = testIec61850Driver.connect(testDeviceAdress, testSettings);
        testIec61850Connection.disconnect();
    }

    @Test
    public void testConnectInvalidSettings3() throws Exception {
        // Test 1
        String testDeviceAdress = "127.0.0.1:54321";
        String testSettings = "-b 12 -lt 1 -rt 1";
        Iec61850Driver testIec61850Driver = new Iec61850Driver();
        thrown.expect(ArgumentSyntaxException.class);
        thrown.expectMessage("Unexpected argument: -b");
        Connection testIec61850Connection = testIec61850Driver.connect(testDeviceAdress, testSettings);
        testIec61850Connection.disconnect();
    }

    @Test
    public void testConnectInvalidAddress1() throws Exception {
        // Test 1
        String testDeviceAdress = "127.0.0.1:54321:foo";
        String testSettings = "-a 12 -lt 1 -rt 1";
        Iec61850Driver testIec61850Driver = new Iec61850Driver();
        thrown.expect(ArgumentSyntaxException.class);
        thrown.expectMessage("Invalid device address syntax.");
        Connection testIec61850Connection = testIec61850Driver.connect(testDeviceAdress, testSettings);
        testIec61850Connection.disconnect();
    }

    @Test
    public void testConnectInvalidAddress2() throws Exception {
        // Test 1
        String testDeviceAdress = "a127.0.0.1:54321";
        String testSettings = "-a 12 -lt 1 -rt 1";
        Iec61850Driver testIec61850Driver = new Iec61850Driver();
        thrown.expect(ConnectionException.class);
        thrown.expectMessage("Unknown host: a127.0.0.1");
        Connection testIec61850Connection = testIec61850Driver.connect(testDeviceAdress, testSettings);
        testIec61850Connection.disconnect();
    }

    @Test
    public void testConnectInvalidAddress3() throws Exception {
        // Test 1
        String testDeviceAdress = "127.0.0.1:foo";
        String testSettings = "-a 12 -lt 1 -rt 1";
        Iec61850Driver testIec61850Driver = new Iec61850Driver();
        thrown.expect(ArgumentSyntaxException.class);
        thrown.expectMessage("The specified port is not an integer");
        Connection testIec61850Connection = testIec61850Driver.connect(testDeviceAdress, testSettings);
        testIec61850Connection.disconnect();
    }

    @AfterEach
    public void closeServerSap() {
        serverSap.stop();
        System.out.println("IED Server stopped");
    }

    private ServerSap runServer(String sclFilePath, int port, ServerSap serverSap, ServerModel serversServerModel)
            throws SclParseException, IOException {

        serverSap = new ServerSap(port, 0, null, SclParser.parse(sclFilePath).get(0), null);
        serverSap.setPort(port);
        serverSap.startListening(this);
        serversServerModel = serverSap.getModelCopy();
        start();
        return serverSap;
    }

    @Override
    public List<ServiceError> write(List<BasicDataAttribute> bdas) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void serverStoppedListening(ServerSap serverSAP) {
        // TODO Auto-generated method stub

    }
}
