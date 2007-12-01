package org.kjkoster.zapcat.example.openfire;

import java.io.File;
import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.util.Log;

/**
 * Example plugin implementation that can be used to make Openfire specific data
 * available for retrieval through JMX. This data can then be queries by Zabbix
 * through the Zapcat plugin.
 * <p>
 * This plugin does nothing more than registering and deregistering a standard
 * Java MBean. This is all that's needed for the Zapcat plugin to be able to
 * query this information.
 * 
 * @author Guus der Kinderen, guus@nimbuzz.com
 */
public class ServerStatsPlugin implements Plugin {
    private ObjectName mbeanName = null;

    /**
     * @see org.jivesoftware.openfire.container.Plugin#initializePlugin(org.jivesoftware.openfire.container.PluginManager,
     *      java.io.File)
     */
    public void initializePlugin(PluginManager manager, File pluginDirectory) {
        Log.info("Initializing ServerStatsPlugin...");

        try {
            // Construct the ObjectName for the MBean that we will register.
            mbeanName = new ObjectName(
                    "com.buzzaa.openfire.plugin.serverstats:type=ServerStats");
            ServerStats mbean = new ServerStats();

            ManagementFactory.getPlatformMBeanServer().registerMBean(mbean,
                    mbeanName);
        } catch (Exception e) {
            Log.error("Unable to register ServerStatsMBean", e);
        }
    }

    /**
     * @see org.jivesoftware.openfire.container.Plugin#destroyPlugin()
     */
    public void destroyPlugin() {
        Log.info("Destroying ServerStatsPlugin...");

        try {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(
                    mbeanName);
        } catch (Exception e) {
            Log.warn("Unable to unregister ServerStatsMBean", e);
        }
    }
}