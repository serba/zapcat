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
import java.util.concurrent.LinkedBlockingQueue;

import org.kjkoster.zapcat.Trapper;

/**
 * A Daemon thread that 'traps' data to a Zabbix server.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public final class ZabbixTrapper implements Trapper {

    private final BlockingQueue<Item> queue;

    private final Sender sender;

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
        queue = new LinkedBlockingQueue<Item>();

        sender = new Sender(queue, InetAddress.getByName(zabbixServer), port,
                host);
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
}
