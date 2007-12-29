package org.kjkoster.zapcat.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.kjkoster.zapcat.zabbix.ZabbixAgent;

/**
 * A servlet that generates the Tomcat Zabbix template. We generate the template
 * for Tomcat because it is so configuraton-dependent. Zabbix really is not able
 * to deal with very dynamic systems.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class ZabbixTemplateServlet extends HttpServlet {
    private static final Logger log = Logger
            .getLogger(ZabbixTemplateServlet.class);

    private enum Type {
        Float, Character, Integer;

        int getValue() {
            switch (this) {
            case Float:
                return 0;
            case Character:
                return 1;
            case Integer:
                return 3;
            }

            throw new IllegalArgumentException("unknown value " + this);
        }
    }

    private enum Time {
        OncePerHour, TwicePerMinute;

        int getValue() {
            switch (this) {
            case OncePerHour:
                return 3600;
            case TwicePerMinute:
                return 30;
            }

            throw new IllegalArgumentException("unknown value " + this);
        }
    }

    private enum Store {
        AsIs, AsDelta
    }

    @Override
    protected void doGet(final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException,
            IOException {
        final PrintWriter out = response.getWriter();
        final MBeanServer mbeanserver = ManagementFactory
                .getPlatformMBeanServer();
        try {
            final Set<ObjectName> managers = (Set<ObjectName>) mbeanserver
                    .queryNames(new ObjectName("Catalina:type=Manager,*"), null);
            final Set<ObjectName> processors = (Set<ObjectName>) mbeanserver
                    .queryNames(new ObjectName(
                            "Catalina:type=GlobalRequestProcessor,*"), null);

            ZabbixTemplateServlet t = new ZabbixTemplateServlet();
            response.setContentType("text/xml"); // XXX
            t.writeHeader(out);
            t.writeItems(out, processors, managers);
            t.writeTriggers(out);
            t.writeGraphs(out, managers);
            t.writeFooter(out);
        } catch (Exception e) {
            log.error("unable to generate template", e);
            e.printStackTrace(out);
        } finally {
            out.flush();
        }
    }

    private void writeHeader(final PrintWriter out) throws UnknownHostException {
        out.println("<?xml version=\"1.0\"?>");
        out.println("<zabbix_export version=\"1.0\" date=\""
                + new SimpleDateFormat("dd.MM.yy").format(new Date())
                + "\" time=\""
                + new SimpleDateFormat("HH.mm").format(new Date()) + "\">");
        out.println("  <hosts>");

        out.println("    <host name=\"tomcat_"
                + InetAddress.getLocalHost().getHostName().replaceAll(
                        "[^a-zA-Z0-9]+", "_") + "\">");
        out.println("      <dns>" + InetAddress.getLocalHost().getHostName()
                + "</dns>");
        out.println("      <ip>" + InetAddress.getLocalHost().getHostAddress()
                + "</ip>");
        out.println("      <port>"
                + System.getProperty(ZabbixAgent.PORT_PROPERTY, ""
                        + ZabbixAgent.DEFAULT_PORT) + "</port>");
        out.println("      <groups>");
        out.println("      </groups>");
    }

    private void writeItems(final PrintWriter out,
            final Set<ObjectName> processors, final Set<ObjectName> managers)
            throws MalformedObjectNameException {
        out.println("      <items>");
        writeItem(out, "tomcat version",
                new ObjectName("Catalina:type=Server"), "serverInfo",
                Type.Character, null, Store.AsIs, Time.OncePerHour);

        writeProcessorItems(out, processors);
        writeManagerItems(out, managers);
        out.println("      </items>");
    }

    private void writeProcessorItems(final PrintWriter out,
            final Set<ObjectName> processors)
            throws MalformedObjectNameException {
        for (final ObjectName processor : processors) {
            final String name = name(processor);

            writeItem(out, name + " bytes received per second", processor,
                    "bytesReceived", Type.Float, "B", Store.AsDelta,
                    Time.TwicePerMinute);
            writeItem(out, name + " bytes sent per second", processor,
                    "bytesSent", Type.Float, "B", Store.AsDelta,
                    Time.TwicePerMinute);
            writeItem(out, name + " requests per second", processor,
                    "requestCount", Type.Float, null, Store.AsDelta,
                    Time.TwicePerMinute);
            writeItem(out, name + " errors per second", processor,
                    "errorCount", Type.Float, null, Store.AsDelta,
                    Time.TwicePerMinute);
            writeItem(out, name + " processing time per second", processor,
                    "processingTime", Type.Float, "s", Store.AsDelta,
                    Time.TwicePerMinute);

            final ObjectName threadpool = new ObjectName(
                    "Catalina:type=ThreadPool,name=" + name);
            writeItem(out, name + " threads max", threadpool, "maxThreads",
                    Type.Integer, null, Store.AsIs, Time.OncePerHour);
            writeItem(out, name + " threads allocated", threadpool,
                    "currentThreadCount", Type.Integer, null, Store.AsIs,
                    Time.TwicePerMinute);
            writeItem(out, name + " threads busy", threadpool,
                    "currentThreadsBusy", Type.Integer, null, Store.AsIs,
                    Time.TwicePerMinute);
        }
    }

    private void writeManagerItems(final PrintWriter out,
            final Set<ObjectName> managers) {
        for (final ObjectName manager : managers) {
            writeItem(out, "sessions " + path(manager) + " active", manager,
                    "activeSessions", Type.Integer, null, Store.AsIs,
                    Time.TwicePerMinute);
            writeItem(out, "sessions " + path(manager) + " peak", manager,
                    "maxActiveSessions", Type.Integer, null, Store.AsIs,
                    Time.TwicePerMinute);
            writeItem(out, "sessions " + path(manager) + " rejected", manager,
                    "rejectedSessions", Type.Integer, null, Store.AsIs,
                    Time.TwicePerMinute);
        }
    }

    private void writeItem(final PrintWriter out, final String description,
            final ObjectName objectname, final String attribute,
            final Type type, final String units, final Store store,
            final Time time) {
        out.println("        <item type=\"0\" key=\"jmx[" + objectname + "]["
                + attribute + "]\" value_type=\"" + type.getValue() + "\">");
        out.println("          <description>" + description + "</description>");
        out.println("          <delay>" + time.getValue() + "</delay>");
        out.println("          <history>90</history>");
        out.println("          <trends>365</trends>");
        if (units != null) {
            out.println("          <units>" + units + "</units>");
        }
        if (store == Store.AsDelta) {
            out.println("          <delta>1</delta>");
        }
        // we assume that all time is logged in milliseconds...
        if ("s".equals(units)) {
            out.println("          <multiplier>1</multiplier>");
            out.println("          <formula>0.001</formula>");

        } else {
            out.println("          <formula>1</formula>");
        }
        out.println("          <snmp_community>public</snmp_community>");
        out
                .println("          <snmp_oid>interfaces.ifTable.ifEntry.ifInOctets.1</snmp_oid>");
        out.println("          <snmp_port>161</snmp_port>");
        out.println("        </item>");
    }

    private void writeTriggers(final PrintWriter out) {
        out.println("      <triggers>");
        out.println("      </triggers>");
    }

    private void writeGraphs(final PrintWriter out,
            final Set<ObjectName> managers) {
        out.println("      <graphs>");
        writeManagerGraphs(out, managers);
        out.println("      </graphs>");
    }

    private void writeManagerGraphs(final PrintWriter out,
            final Set<ObjectName> managers) {
        for (final ObjectName manager : managers) {
            out.println("        <graph name=\"sessions " + path(manager)
                    + "\" width=\"900\" height=\"200\">");
            out.println("          <show_work_period>1</show_work_period>");
            out.println("          <show_triggers>1</show_triggers>");
            out.println("          <yaxismin>0.0000</yaxismin>");
            out.println("          <yaxismax>100.0000</yaxismax>");
            out.println("          <graph_elements>");
            out.println("            <graph_element item=\"{HOSTNAME}:jmx["
                    + manager + "][rejectedSessions]\">");
            out.println("              <color>990099</color>");
            out.println("              <yaxisside>1</yaxisside>");
            out.println("              <calc_fnc>2</calc_fnc>");
            out.println("              <periods_cnt>5</periods_cnt>");
            out.println("            </graph_element>");
            out.println("            <graph_element item=\"{HOSTNAME}:jmx["
                    + manager + "][maxActiveSessions]\">");
            out.println("              <color>990000</color>");
            out.println("              <yaxisside>1</yaxisside>");
            out.println("              <calc_fnc>2</calc_fnc>");
            out.println("              <periods_cnt>5</periods_cnt>");
            out.println("            </graph_element>");
            out.println("            <graph_element item=\"{HOSTNAME}:jmx["
                    + manager + "][activeSessions]\">");
            out.println("              <color>009900</color>");
            out.println("              <yaxisside>1</yaxisside>");
            out.println("              <calc_fnc>2</calc_fnc>");
            out.println("              <periods_cnt>5</periods_cnt>");
            out.println("            </graph_element>");
            out.println("          </graph_elements>");
            out.println("        </graph>");
        }
    }

    private String path(final ObjectName objectname) {
        final String name = objectname.toString();
        final int start = name.indexOf("path=") + 5;
        final int end = name.indexOf(',', start);

        return name.substring(start, end);
    }

    private String name(final ObjectName objectname) {
        final String name = objectname.toString();
        final int start = name.indexOf("name=") + 5;

        return name.substring(start);
    }

    private void writeFooter(final PrintWriter out) {
        out.println("    </host>");
        out.println("  </hosts>");
        out.println("</zabbix_export>");
        out.println();
    }
}
