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

import java.util.StringTokenizer;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.apache.log4j.Logger;

/**
 * A helper class that abstracts from JMX.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public final class JMXHelper {
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

		final MBeanServer mbeanserver = getMBeanServer();
		
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

    /**
     * Invoke a JMX operation by providing the mbean name, the operation name and arguments.
     * 
     * @param name
     *            The object name of the mbean to invoke operation on.
     * @param operation
     *            The operation to invoke.
     * @param query_args
     * 				The arguments to pass to the operation.
     * @return A String representation of the object returned by invoking the operation.
     * 
     * @throws Exception
     *             UnsupportedOperationException
     *             
     * This is based on the patch submitted anonymously to the project page on sourceforge 
     */
	public static String op_query(final String name, final String operation,
			final String query_args) throws Exception {
		log.debug("JMX query[" + name + "][" + operation + "]"
				+ query_args.substring(1));

		final MBeanServer mbeanserver = getMBeanServer();
		
		final MBeanInfo info = mbeanserver.getMBeanInfo(new ObjectName(name));
		final MBeanOperationInfo[] ops = info.getOperations();
		StringTokenizer tokens = new StringTokenizer(query_args, "[],", false);

		int op_index = -1;
		boolean op_name_exist = false;
		MBeanParameterInfo[] signature = null;

		for (int i = 0; i < ops.length; i++) {
			if (ops[i].getName().equalsIgnoreCase(operation)) {
				op_name_exist = true;
				signature = ops[i].getSignature();
				if (signature.length == tokens.countTokens()) {
					op_index = i;
					break;
				}
			}
		}
		if (op_index == -1 && op_name_exist) {
			throw new UnsupportedOperationException(
					"number of argument does not match");
		} else if (op_index == -1 && !op_name_exist) {
			throw new UnsupportedOperationException(
					"operation specified is not found in mbean");
		}

		String[] string_sig = new String[signature.length];
		Object[] obj_args = new Object[signature.length];
		for (int j = 0; j < signature.length; j++) {
			string_sig[j] = signature[j].getType();
			if (string_sig[j].equals("long")) {
				obj_args[j] = new Long(tokens.nextToken());
			} else if (string_sig[j].equals("int")) {
				obj_args[j] = new Integer(tokens.nextToken());
			} else if (string_sig[j].equals("java.lang.String")) {
				obj_args[j] = new String(tokens.nextToken());
			} else if (string_sig[j].equals("boolean")) {
				obj_args[j] = new Boolean(tokens.nextToken());
			} else if (string_sig[j].equals("float")) {
				obj_args[j] = new Float(tokens.nextToken());
			} else if (string_sig[j].equals("double")) {
				obj_args[j] = new Double(tokens.nextToken());
			}
		}
		return mbeanserver.invoke(new ObjectName(name), operation, obj_args,
				string_sig).toString();
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
	
	/**
	 * Determine the J2EE container and return the relevant mbeanserver. 
	 * 
	 * Will return ManagementFactory.getPlatformMBeanServer for everything
	 * except JBoss (4.2 only?), in which case the JBoss-specific server
	 * will be returned. Additional support for other J2EE containers will
	 * follow.
	 * 
	 * @author brett cave
	 * 
	 * @return the MBean server.
	 */
	public static MBeanServer getMBeanServer() {
    	if (isJboss())
    		return org.jboss.mx.util.MBeanServerLocator.locateJBoss();
    	else
    		return java.lang.management.ManagementFactory.getPlatformMBeanServer();
    }
    
	/**
	 * Determines if this is deployed in a JBoss (4.2) j2ee container, based
	 * on whether the jboss.partition.name key exists in the Runtime mbean's
	 * SystemProperties attribute. There may be a better way, as yet undetermined.
	 *  
	 *  @author brett cave
	 *  
	 * @return boolean true if this is running in a JBoss container.
	 */
    private static boolean isJboss() {
    	try {
    		TabularData result = (TabularData)java.lang.management.ManagementFactory.getPlatformMBeanServer().getAttribute(new ObjectName("java.lang:type=Runtime"), "SystemProperties");
    		Object[] key = {"jboss.partition.name"};
    		return result.containsKey(key);
    	}
    	catch (Exception e) {
    		return false;
    	}
    }

	/**
	 * Try to register a managed bean. Note that errors are logged but then
	 * suppressed.
	 * 
	 * @param mbean
	 *            The managed bean to register.
	 * @param objectName
	 *            The name under which to register the bean.
	 * @return ObjectName The object name of the mbean, for later deregistration.
	 */
	public static ObjectName register(final Object mbean,
			final String objectName) {
		log.debug("registering [" + objectName + "]: " + mbean);

		final MBeanServer mbeanserver = getMBeanServer();
		
		ObjectName name = null;
		try {
			name = new ObjectName(objectName);
			mbeanserver.registerMBean(mbean, name);
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

		final MBeanServer mbeanserver = getMBeanServer();
		
		try {
			mbeanserver.unregisterMBean(objectName);
		} catch (InstanceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			log.warn("unable to unregister '" + objectName + "'", e);
		}
	}
}
