package org.kjkoster.zapcat;

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

/**
 * The interface of a trapper and sender. Trappers and senders take initiative
 * in sending data to the monitoring server.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public interface Trapper {

    /**
     * Send a value to the monitoring server immediately. This method will not
     * accept <code>null</code> values.
     * 
     * @param key
     *            The identifier of the data item.
     * @param value
     *            The value. Cannot be <code>null</code>.
     */
    void send(String key, Object value);

    /**
     * Stop the trapper and clean up.
     */
    void stop();
}