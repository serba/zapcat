package org.jivesoftware.util;

import java.util.Map;

/**
 * A plugin interface stub. This code was reverse engineered from the zapcat
 * plugin, and is not derived from Jive Software's code.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public interface PropertyEventListener {

    void propertySet(String property, Map<String, Object> params);

    void propertyDeleted(String property, Map<String, Object> params);

    void xmlPropertySet(String property, Map<String, Object> params);

    void xmlPropertyDeleted(String property, Map<String, Object> params);
}
