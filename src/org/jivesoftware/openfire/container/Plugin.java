package org.jivesoftware.openfire.container;

import java.io.File;

/**
 * A plugin interface stub. This code was reverse engineered from the zapcat
 * plugin, and is not derived from Jive Software's code.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public interface Plugin {

    void initializePlugin(PluginManager manager, File pluginDirectory);

    void destroyPlugin();
}
