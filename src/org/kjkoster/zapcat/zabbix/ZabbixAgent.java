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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.kjkoster.zapcat.Agent;

/**
 * A passive Zabbix agent. This agent starts and manages a daemon thread.
 * <p>
 * The agent uses an executor service to handle the JMX queries that come in.
 * This allows us to handle a few queries concurrently.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public final class ZabbixAgent implements Agent, Runnable {
    private static final Logger log = Logger.getLogger(ZabbixAgent.class);

    private final int port;

    private final Thread daemon;

    private ServerSocket listener = null;

    private final ExecutorService handlers;

    private boolean closing = false;

    /**
     * Configure a new Zabbix agent. Each agent needs the local port number to
     * run. This constructor configures the port number by checking for a system
     * property named "org.kjkoster.zapcat.zabbix.port". If it is not there, the
     * port number defaults to 10052.
     */
    public ZabbixAgent() {
        this(Integer.parseInt(System.getProperty(
                "org.kjkoster.zapcat.zabbix.port", "10052")));
    }

    /**
     * Configure a new Zabbix agent to listen on a specific port number.
     * <p>
     * Use of this method is discouraged, since it places port number
     * configuration in the hands of developers. That is a task that should
     * normally be left to system administrators and done through system
     * properties.
     * 
     * @param port
     *            The port number to listen on.
     */
    public ZabbixAgent(final int port) {
        this.port = port;
        handlers = new ThreadPoolExecutor(1, 5, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());

        daemon = new Thread(this, "Zabbix-agent");
        daemon.setDaemon(true);
        daemon.start();
    }

    /**
     * Stop and clean up the used resources. I am not entirely happy with the
     * way we close the server socket.
     * 
     * @see org.kjkoster.zapcat.Agent#stop()
     */
    public void stop() {
        closing = true;

        try {
            final ServerSocket toClose = listener;
            listener = null;
            toClose.close();
        } catch (IOException e) {
            // ignore, we're going down anyway...
        }
        try {
            daemon.join();
        } catch (InterruptedException e) {
            // ignore, we're going down anyway...
        }

        try {
            handlers.shutdown();
            handlers.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // ignore, we're going down anyway...
        }
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            listener = new ServerSocket(port);
            log.debug("listening on port " + listener.getLocalPort());

            for (;;) {
                final Socket accepted = listener.accept();
                log.debug("accepted connection from "
                        + accepted.getInetAddress().getHostAddress());

                handlers.execute(new QueryHandler(accepted));
            }
        } catch (IOException e) {
            if (!closing) {
                log.error("caught exception, exiting", e);
            }
        } finally {
            if (listener != null) {
                try {
                    listener.close();
                } catch (IOException e) {
                    // ignore, we're going down anyway...
                }
            }
            log.debug("zabbix agent exits");
        }
    }
}
