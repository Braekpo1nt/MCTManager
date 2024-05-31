package org.braekpo1nt.mctmanager.games.game.interfaces;

import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;

public interface Configurable {
    /**
     * Load the config file. If the config file fails to load, throws an {@link ConfigException}.
     * @throws ConfigInvalidException if the loaded config is invalid
     * @throws ConfigIOException if there are any IO errors when loading the config.
     */
    void loadConfig() throws ConfigIOException, ConfigInvalidException;
}
