package org.kjkoster.zapcat.servlet;

import java.net.UnknownHostException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.kjkoster.zapcat.Agent;
import org.kjkoster.zapcat.zabbix.ZabbixAgent;

/**
 * A servlet that starts the Zabbix agent.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public class ZapcatServlet extends HttpServlet {

    private Agent agent = null;

    /**
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init() throws ServletException {
        try {
            agent = new ZabbixAgent();
        } catch (UnknownHostException e) {
            throw new ServletException(e);
        }
    }

    /**
     * @see javax.servlet.GenericServlet#destroy()
     */
    @Override
    public void destroy() {
        agent.stop();
    }
}
