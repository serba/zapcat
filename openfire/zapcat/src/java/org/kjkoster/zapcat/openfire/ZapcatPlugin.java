package org.kjkoster.zapcat.openfire;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.Log;
import org.jivesoftware.util.PropertyEventDispatcher;
import org.jivesoftware.util.PropertyEventListener;
import org.kjkoster.zapcat.Agent;
import org.kjkoster.zapcat.zabbix.ZabbixAgent;

/**
 * This plugin enables a Zabbix server to query Openfire in a JMX style.
 * 
 * @author Guus der Kinderen, guus@nimbuzz.com
 */
public class ZapcatPlugin implements Plugin, PropertyEventListener {
    private static final int ZAPCAT_PORT_DEFAULT = 10052;

    private static final String ZAPCAT_PORT = "zapcat.port";

    private static final String ZAPCAT_INETADDRESS = "zapcat.inetaddress";

    private Agent agent = null;

    private InetAddress inetAddress = null;

    private int port = ZAPCAT_PORT_DEFAULT;

    /*
     * (non-Javadoc) Initializing
     * 
     * @see org.jivesoftware.openfire.container.Plugin#initializePlugin(org.jivesoftware.openfire.container.PluginManager,
     *      java.io.File)
     */
    @Override
    public void initializePlugin(PluginManager manager, File pluginDirectory) {
        Log.info("Initializing Zapcat Plugin.");

        PropertyEventDispatcher.addListener(this);
        final String address = JiveGlobals.getProperty(ZAPCAT_INETADDRESS);
        if (address != null) {
            try {
                inetAddress = InetAddress.getByName(address);
            } catch (UnknownHostException ex) {
                Log.warn("Unable to parse InetAddress from property. "
                        + "Using default value instead.", ex);
                inetAddress = null;
            }
        }

        port = JiveGlobals.getIntProperty(ZAPCAT_PORT, ZAPCAT_PORT_DEFAULT);
        agent = new ZabbixAgent(inetAddress, port);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jivesoftware.openfire.container.Plugin#destroyPlugin()
     */
    @Override
    public void destroyPlugin() {
        Log.info("Destroying Zapcat Plugin.");
        PropertyEventDispatcher.removeListener(this);

        if (agent != null) {
            agent.stop();
        }
    }

    /**
     * Reloads the agent with the new attributes. This method will not reload
     * the agent if the new attributes are no different from the old ones. This
     * method will restart the Agent if no attribute change was detected, but
     * the Agent was not running.
     * 
     * @param addr
     *                The address to listen on, or 'null' to listen on any
     *                available address.
     * @param p
     *                The port number to listen on.
     */
    private void restartAgent(final InetAddress addr, final int p) {
        boolean restartAgent = false;
        // check if this changes the current value.
        if (addr != null && !addr.equals(inetAddress)) {
            inetAddress = addr;
            restartAgent = true;
        } else if (inetAddress != null && !inetAddress.equals(addr)) {
            inetAddress = addr;
            restartAgent = true;
        }

        if (port != p) {
            port = p;
            restartAgent = true;
        }

        if (!restartAgent && agent != null) {
            // no need to change anything.
            return;
        }

        if (agent != null) {
            Log.debug("Stopping agent that's currently running.");
            agent.stop();
        }

        Log.debug("Starting new agent.");
        agent = new ZabbixAgent(inetAddress, port);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jivesoftware.util.PropertyEventListener#propertySet(java.lang.String,
     *      java.util.Map)
     */
    @Override
    public void propertySet(String property, Map<String, Object> params) {
        if (ZAPCAT_INETADDRESS.equals(property)) {
            final String addr = (String) params.get("value");
            try {
                final InetAddress iAddr;
                if (addr.isEmpty()) {
                    iAddr = null;
                } else {
                    iAddr = InetAddress.getByName(addr);
                }
                restartAgent(iAddr, port);
            } catch (UnknownHostException ex) {
                Log.warn("Unable to parse inetaddress from new property. "
                        + "Using old value instead.", ex);
            }
        } else if (ZAPCAT_PORT.equals(property)) {
            restartAgent(inetAddress, Integer.parseInt((String) params
                    .get("value")));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jivesoftware.util.PropertyEventListener#propertyDeleted(java.lang.String,
     *      java.util.Map)
     */
    @Override
    public void propertyDeleted(String property, Map<String, Object> params) {
        if (ZAPCAT_INETADDRESS.equals(property)) {
            restartAgent(null, port);
        } else if (ZAPCAT_PORT.equals(property)) {
            restartAgent(inetAddress, ZAPCAT_PORT_DEFAULT);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jivesoftware.util.PropertyEventListener#xmlPropertySet(java.lang.String,
     *      java.util.Map)
     */
    @Override
    public void xmlPropertySet(String property, Map<String, Object> params) {
        // not used.
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jivesoftware.util.PropertyEventListener#xmlPropertyDeleted(java.lang.String,
     *      java.util.Map)
     */
    @Override
    public void xmlPropertyDeleted(String property, Map<String, Object> params) {
        // not used.
    }
}
