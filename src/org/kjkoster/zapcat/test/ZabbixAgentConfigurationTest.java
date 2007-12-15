package org.kjkoster.zapcat.test;

/* This file is part of Zapcat.
 *
 * Zapcat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Zapcat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Zapcat.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;

import javax.management.InstanceNotFoundException;

import junit.framework.TestCase;

import org.kjkoster.zapcat.Agent;
import org.kjkoster.zapcat.zabbix.JMXHelper;
import org.kjkoster.zapcat.zabbix.ZabbixAgent;

/**
 * Test cases to test the configuration options of the Zabbix agent.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class ZabbixAgentConfigurationTest extends TestCase {
    private static final int DEFAULTPORT = ZabbixAgent.DEFAULT_PORT;

    private static final int ARGUMENTPORT = 10053;

    private static final int PROPERTYPORT = 10054;

    private final Properties originalProperties;

    /**
     * Set up the test, preserving the system's configuration.
     */
    public ZabbixAgentConfigurationTest() {
        super();

        originalProperties = System.getProperties();
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        System.setProperties(originalProperties);
        assertNull(System.getProperty(ZabbixAgent.PORT_PROPERTY));
        assertNull(System.getProperty(ZabbixAgent.ADDRESS_PROPERTY));

        assertAgentDown(DEFAULTPORT);
        assertAgentDown(ARGUMENTPORT);
        assertAgentDown(PROPERTYPORT);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        assertAgentDown(DEFAULTPORT);
        assertAgentDown(ARGUMENTPORT);
        assertAgentDown(PROPERTYPORT);
    }

    /**
     * Test that we can start and stop the agent.
     * 
     * @throws Exception
     *             When the test failed.
     */
    public void testStartAndStop() throws Exception {
        final Agent agent = new ZabbixAgent();

        assertAgentUp(DEFAULTPORT);
        assertAgentDown(ARGUMENTPORT);
        assertAgentDown(PROPERTYPORT);

        agent.stop();
    }

    /**
     * Test that the agent can handle connection errors.
     * 
     * @throws Exception
     *             When the test failed.
     */
    public void testTouchAndTouch() throws Exception {
        final Agent agent = new ZabbixAgent();

        assertAgentUp(DEFAULTPORT);
        assertAgentUp(DEFAULTPORT);

        assertAgentDown(ARGUMENTPORT);
        assertAgentDown(PROPERTYPORT);

        agent.stop();
    }

    /**
     * Test that we can use a Java argument to configure the port number on the
     * agent. The argument should override the default port number.
     * 
     * @throws Exception
     *             When the test failed.
     */
    public void testPortAsArgument() throws Exception {
        final Agent agent = new ZabbixAgent(ARGUMENTPORT);

        assertAgentDown(DEFAULTPORT);
        assertAgentUp(ARGUMENTPORT);
        assertAgentDown(PROPERTYPORT);

        agent.stop();
    }

    /**
     * Test that we can use a Java system property to configure the port number
     * on the agent. The property should override the default port number and
     * the number passed as an argument.
     * 
     * @throws Exception
     *             When the test failed.
     */
    public void testPortAsProperty() throws Exception {
        final Properties testProperties = (Properties) originalProperties
                .clone();
        testProperties.setProperty(ZabbixAgent.PORT_PROPERTY, ""
                + PROPERTYPORT);
        System.setProperties(testProperties);
        assertEquals("" + PROPERTYPORT, System
                .getProperty(ZabbixAgent.PORT_PROPERTY));

        final Agent agent = new ZabbixAgent(ARGUMENTPORT);

        assertAgentDown(DEFAULTPORT);
        assertAgentDown(ARGUMENTPORT);
        assertAgentUp(PROPERTYPORT);

        agent.stop();
    }

    private void assertAgentDown(final int port) throws Exception {
        try {
            JMXHelper.query("org.kjkoster.zapcat:type=Agent,port=" + port,
                    "Port");
            fail("agent mbean found, but not expected");
        } catch (InstanceNotFoundException e) {
            // this is supposed to happen
        }
        try {
            new Socket(InetAddress.getLocalHost(), port);
            fail("port " + port + " is in use");
        } catch (ConnectException e) {
            // this is supposed to happen
        }
    }

    private void assertAgentUp(final int port) throws Exception {
        // give the agent some time to open the port
        Thread.sleep(100);

        assertEquals("" + port, JMXHelper.query(
                "org.kjkoster.zapcat:type=Agent,port=" + port, "Port"));
        assertEquals("*", JMXHelper.query(
                "org.kjkoster.zapcat:type=Agent,port=" + port, "BindAddress"));
        final Socket touch = new Socket(InetAddress.getLocalHost(), port);
        touch.close();
    }
}
