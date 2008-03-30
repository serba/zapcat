package org.kjkoster.zapcat.zabbix;

/* This file is part of Zapcat.
 *
 * Zapcat is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Zapcat is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Zapcat. If not, see <http://www.gnu.org/licenses/>.
 */

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.apache.log4j.Logger;

/**
 * A helper class that abstracts from JMX.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public final class JMXHelper {
    private static final Logger log = Logger.getLogger(JMXHelper.class);

    private static MBeanServer mbeanserver = null;

    /**
     * Locate the mbean server for this JVM instance. We try to look for the
     * JBoss specific mbean server. Failing that, we just use the JVM's platorm
     * mbean server.
     * 
     * @return An appropriate mbean server.
     */
    public static MBeanServer getMBeanServer() {
        if (mbeanserver == null) {
            // first, we try to see if we are running in JBoss
            try {
                mbeanserver = (MBeanServer) Class.forName(
                        "org.jboss.mx.util.MBeanServerLocator").getMethod(
                        "locateJBoss", (Class[]) null).invoke(null,
                        (Object[]) null);
            } catch (Exception e) {
                // woops: not JBoss. Use the platform mbean server instead
                mbeanserver = ManagementFactory.getPlatformMBeanServer();
            }
        }

        return mbeanserver;
    }

    /**
     * Perform a JMX query given an mbean name and the name of an attribute on
     * that mbean.
     * 
     * @param objectName
     *            The object name of the mbean to query.
     * @param attribute
     *            The attribute to query for.
     * @return The value of the attribute.
     * @throws Exception
     *             When something went wrong.
     */
    public static String query(final ObjectName objectName,
            final String attribute) throws Exception {
        log.debug("JMX query[" + objectName + "][" + attribute + "]");

        final ObjectInstance bean = getMBeanServer().getObjectInstance(
                objectName);
        log.debug("found MBean class " + bean.getClassName());

        final int dot = attribute.indexOf('.');
        if (dot < 0) {
            final Object ret = getMBeanServer().getAttribute(objectName,
                    attribute);
            return ret == null ? null : ret.toString();
        }

        return resolveFields((CompositeData) getMBeanServer().getAttribute(
                objectName, attribute.substring(0, dot)), attribute
                .substring(dot + 1));
    }

    private static String resolveFields(final CompositeData attribute,
            final String field) {
        final int dot = field.indexOf('.');
        if (dot < 0) {
            final Object ret = attribute.get(field);
            return ret == null ? null : ret.toString();
        }

        return resolveFields((CompositeData) attribute.get(field.substring(0,
                dot)), field.substring(dot + 1));
    }

    /**
     * Try to register a managed bean. Note that errors are logged but then
     * suppressed.
     * 
     * @param mbean
     *            The managed bean to register.
     * @param objectName
     *            The name under which to register the bean.
     * @return The object name of the mbean, for later deregistration.
     */
    public static ObjectName register(final Object mbean,
            final String objectName) {
        log.debug("registering [" + objectName + "]: " + mbean);

        ObjectName name = null;
        try {
            name = new ObjectName(objectName);
            getMBeanServer().registerMBean(mbean, name);
        } catch (Exception e) {
            log.warn("unable to register '" + name + "'", e);
        }

        return name;
    }

    /**
     * Remove the registration of a bean.
     * 
     * @param objectName
     *            The name of the bean to unregister.
     */
    public static void unregister(final ObjectName objectName) {
        log.debug("un-registering [" + objectName + "]");

        try {
            getMBeanServer().unregisterMBean(objectName);
        } catch (Exception e) {
            log.warn("unable to unregister '" + objectName + "'", e);
        }
    }
}
