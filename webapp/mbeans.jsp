<%@ page import="java.lang.management.ManagementFactory"%>
<%@ page import="java.lang.reflect.Method"%>
<%@ page import="java.util.Set"%>
<%@ page import="javax.management.*"%>
<%@ page import="javax.management.openmbean.CompositeData"%>

<html>
<head>
<title>MBean List</title>
<link href="zapcat.css" rel="stylesheet" type="text/css">
</head>
<body>
<h1>MBean List</h1>
<p>This page gives you a list of all the mbeans and their attributes
that are available in this JVM. This information is formatted so that it
you can easily cut-and-paste them into the Zabbix configuration screens.
Their type information is provided, but information about their meaning
and significance can only be found on the Internet and in the various
forums.</p>
<p>Please be warned that the number and type of available mbeans may
change with different hardware, operating systems, JVM versions, JVM
configuration, Tomcat versions and Tomcat configuration options.</p>
<p>And yes, there are a lot of them. :-)</p>

<table>
	<%
	    final MBeanServer mbeanserver = ManagementFactory
	            .getPlatformMBeanServer();

	    for (final ObjectName mbean : (Set<ObjectName>) mbeanserver
	            .queryNames(null, null)) {
	%>
	<tr>
		<td class="mbean" colspan="3">jmx[<%=mbean%>]</td>
	</tr>
	<%
	    final MBeanInfo info = mbeanserver.getMBeanInfo(mbean);
	        for (final MBeanAttributeInfo attrib : info.getAttributes()) {
	%>
	<tr>
		<td>&nbsp;&nbsp;</td>
		<td class="attrib">[<%=attrib.getName()%>]</td>
		<td class="attrib"><%=zabbixType(attrib.getType())%></td>
	</tr>
	<%
	    }
	    }
	%>
</table>
</body>

<%!private static String zabbixType(final String type) {
        if (type.equals("java.lang.String")
                || type.equals("javax.management.ObjectName")) {
            return "text";
        }
        if (type.equals("java.lang.Short") || type.equals("short")
                || type.equals("java.lang.Integer") || type.equals("int")
                || type.equals("java.lang.Long") || type.equals("long")) {
            return "numeric (integer 64bit)";
        }
        if (type.equals("java.lang.Float") || type.equals("float")
                || type.equals("java.lang.Double") || type.equals("double")) {
            return "numeric (float)";
        }

        return "unknown&nbsp;("
                + type
                + "),&nbsp;but&nbsp;you&nbsp;can&nbsp;try&nbsp;using&nbsp;'text'.";
    }%>