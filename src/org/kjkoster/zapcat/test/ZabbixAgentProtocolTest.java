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

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;

import junit.framework.TestCase;

import org.kjkoster.zapcat.Agent;
import org.kjkoster.zapcat.zabbix.ZabbixAgent;

/**
 * A test case to test that the protocol configuration works.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class ZabbixAgentProtocolTest extends TestCase {

    private final Properties originalProperties;

    /**
     * Set up the test, preserving the system's configuration.
     */
    public ZabbixAgentProtocolTest() {
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
        assertNull(System.getProperty(ZabbixAgent.PROTOCOL_PROPERTY));
    }

    /**
     * Test that by default we have protocol version 1.4.
     * 
     * @throws Exception
     *             When the test failed.
     */
    public void testDefault() throws Exception {
        final Agent agent = new ZabbixAgent();
        // give the agent some time to open the port
        Thread.sleep(100);
        final Socket socket = new Socket(InetAddress.getLocalHost(),
                ZabbixAgent.DEFAULT_PORT);

        final Writer out = new OutputStreamWriter(socket.getOutputStream());
        out.write("system.property[java.version]\n");
        out.flush();

        final InputStream in = socket.getInputStream();
        final byte[] buffer = new byte[1024];
        in.read(buffer);

        assertEquals('Z', buffer[0]);
        assertEquals('B', buffer[1]);
        assertEquals('X', buffer[2]);
        assertEquals('D', buffer[3]);
        // we'll take the rest for granted...

        socket.close();
        agent.stop();
    }

    /**
     * Test that we can use a Java system property to configure the protocol
     * version on the agent.
     * 
     * @throws Exception
     *             When the test failed.
     */
    public void testSetTo14() throws Exception {
        final Properties testProperties = (Properties) originalProperties
                .clone();
        testProperties.setProperty(ZabbixAgent.PROTOCOL_PROPERTY, "1.4");
        System.setProperties(testProperties);
        assertEquals("1.4", System.getProperty(ZabbixAgent.PROTOCOL_PROPERTY));

        testDefault();
    }

    /**
     * Test that we can use a Java system property to configure the protocol
     * version on the agent.
     * 
     * @throws Exception
     *             When the test failed.
     */
    public void testSetTo11() throws Exception {
        final Properties testProperties = (Properties) originalProperties
                .clone();
        testProperties.setProperty(ZabbixAgent.PROTOCOL_PROPERTY, "1.1");
        System.setProperties(testProperties);
        assertEquals("1.1", System.getProperty(ZabbixAgent.PROTOCOL_PROPERTY));

        final Agent agent = new ZabbixAgent();
        // give the agent some time to open the port
        Thread.sleep(100);
        final Socket socket = new Socket(InetAddress.getLocalHost(),
                ZabbixAgent.DEFAULT_PORT);

        final Writer out = new OutputStreamWriter(socket.getOutputStream());
        out.write("system.property[java.version]\n");
        out.flush();

        final InputStream in = socket.getInputStream();
        final byte[] buffer = new byte[1024];
        in.read(buffer);

        final String version = System.getProperty("java.version");

        assertEquals(version.charAt(0), buffer[0]);
        assertEquals(version.charAt(1), buffer[1]);
        assertEquals(version.charAt(2), buffer[2]);
        assertEquals(version.charAt(3), buffer[3]);
        // we'll take the rest for granted...

        socket.close();
        agent.stop();
    }
}
