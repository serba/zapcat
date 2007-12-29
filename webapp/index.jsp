<%@ page import="org.kjkoster.zapcat.zabbix.JMXHelper"%>
<%
    final String objectName = "org.kjkoster.zapcat:type=Agent,port="
            + System.getProperty("org.kjkoster.zapcat.zabbix.port",
                    "10052");
    final String port = JMXHelper.query(objectName, "Port");
    String address = JMXHelper.query(objectName, "BindAddress");
    if (address.equals("*")) {
        address = "all available addresses";
    }
%>
<html>
<head>
<title>Welcome to Zapcat</title>
<link href="zapcat.css" rel="stylesheet" type="text/css">
</head>
<body>
<h1>Welcome to Zapcat</h1>
<p>Welcome to the Zapcat servlet engine plugin. This plugin is the
quickest way to enable Zapcat on a servlet engine such as Tomcat or
JBoss.</p>
<p>The Zapcat agent is listening on port&nbsp;<%=port%>, and bound
to <%=address%>.</p>
<p>If you use Tomcat, there is an official Zapcat for <a
	href="http://www.kjkoster.org/zapcat/Tomcat_How_To.html">Tomcat
how-to</a>. On that page, you will find step-by-step instructions that guide
you through the process of <a href="zabbix-tomcat-definition.xml">generating
a custom host definition</a> and hooking Zabbix up to your application
server. Generating the host definition only works in Tomcat application
servers.</p>
<p>If you would like to add your own statistics to you host
definition, the <a href="mbeans.jsp">mbean list</a> is a page that gives
you a conveniently formatted list of all the mbeans in your server.</p>
</body>
</html>
