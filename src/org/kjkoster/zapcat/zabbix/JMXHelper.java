package org.kjkoster.zapcat.zabbix;

/* This file is part of Zapcat.
 *
 * Zapcat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 * Zapcat is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Zapcat.  If not, see <http://www.gnu.org/licenses/>.
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
final class JMXHelper {
    private static final Logger log = Logger.getLogger(JMXHelper.class);

    /**
     * Perform a JMX query given an mbean name and the name of an attribute on
     * that mbean.
     * 
     * @param name
     *            The object name of the mbean to query.
     * @param attribute
     *            The attribute to query for.
     * @return The value of the attribute.
     * @throws Exception
     *             When something went wrong.
     */
    public static String query(final String name, final String attribute)
            throws Exception {
        log.debug("JMX query[" + name + "][" + attribute + "]");

        final MBeanServer mbeanserver = ManagementFactory
                .getPlatformMBeanServer();

        final ObjectInstance bean = mbeanserver
                .getObjectInstance(new ObjectName(name));
        log.debug("found MBean class " + bean.getClassName());

        final int dot = attribute.indexOf('.');
        if (dot < 0) {
            final Object ret = mbeanserver.getAttribute(new ObjectName(name),
                    attribute);
            return ret == null ? null : ret.toString();
        }

        return resolveFields((CompositeData) mbeanserver.getAttribute(
                new ObjectName(name), attribute.substring(0, dot)), attribute
                .substring(dot + 1));
    }

    private static String resolveFields(final CompositeData attribute,
            final String field) throws Exception {
        final int dot = field.indexOf('.');
        if (dot < 0) {
            final Object ret = attribute.get(field);
            return ret == null ? null : ret.toString();
        }

        return resolveFields((CompositeData) attribute.get(field.substring(0,
                dot)), field.substring(dot + 1));
    }
}