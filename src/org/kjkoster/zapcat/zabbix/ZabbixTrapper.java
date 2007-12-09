package org.kjkoster.zapcat.zabbix;

/* This file is part of Zapcat.
 *
 * Zapcat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Zapcat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Zapcat.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.kjkoster.zapcat.Trapper;

/**
 * A Daemon thread that 'traps' data to a Zabbix server.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public final class ZabbixTrapper implements Trapper {

    private final BlockingQueue<Item> queue = new LinkedBlockingQueue<Item>();

    private final Sender sender;

    private final ScheduledExecutorService scheduler = Executors
            .newScheduledThreadPool(1);

    /**
     * Create a new Zabbix trapper.
     * 
     * @param zabbixServer
     *            The name or IP address of the machine that Zabbix runs on.
     * @param port
     *            The port number on that machine.
     * @param host
     *            The name of the host as defined in the hosts section in
     *            Zabbix.
     * @throws UnknownHostException
     *             When the zabbix server name could not be resolved.
     */
    public ZabbixTrapper(final String zabbixServer, final int port,
            final String host) throws UnknownHostException {
        final String server = System.getProperty(
                "org.kjkoster.zapcat.zabbix.server", zabbixServer);
        final String serverPort = System
                .getProperty("org.kjkoster.zapcat.zabbix.serverport", Integer
                        .toString(port));
        final String serverHost = System.getProperty(
                "org.kjkoster.zapcat.zabbix.host", host);

        sender = new Sender(queue, InetAddress.getByName(server), Integer
                .parseInt(serverPort), serverHost);
        sender.start();
    }

    /**
     * Create a new Zabbix trapper, using the default port number.
     * 
     * @param zabbixServer
     *            The name or IP address of the machine that Zabbix runs on.
     * @param host
     *            The name of the host as defined in the hosts section in
     *            Zabbix.
     * @throws UnknownHostException
     *             When the zabbix server name could not be resolved.
     */
    public ZabbixTrapper(final String zabbixServer, final String host)
            throws UnknownHostException {
        this(zabbixServer, 10051, host);
    }

    /**
     * @see org.kjkoster.zapcat.Trapper#stop()
     */
    public void stop() {
        sender.stopping();
        try {
            sender.join();
        } catch (InterruptedException e) {
            // ignore, we're done anyway...
        }
    }

    /**
     * @see org.kjkoster.zapcat.Trapper#send(java.lang.String, java.lang.Object)
     */
    public void send(final String key, final Object value) {
        queue.offer(new Item(key, value.toString()));
    }

    /**
     * @see org.kjkoster.zapcat.Trapper#send(java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    public void send(final String key, final String objectName,
            final String attribute) {
        queue.offer(new Item(key, objectName, attribute));
    }

    /**
     * @see org.kjkoster.zapcat.Trapper#every(int,
     *      java.util.concurrent.TimeUnit, java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    public void every(final int time, final TimeUnit unit, final String key,
            final String objectName, final String attribute) {
        scheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                send(key, objectName, attribute);
            }
        }, 0, time, unit);
    }
}
