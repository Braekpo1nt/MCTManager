package org.braekpo1nt.mctmanager.games.game.interfaces;

import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.jetbrains.annotations.NotNull;

public interface Configurable {
    /**
     * Load the config file. If the config file fails to load, throws an {@link ConfigException}.
     * @param configFile the json file (including json extension) that the config is in
     * @throws ConfigInvalidException if the loaded config is invalid
     * @throws ConfigIOException if there are any IO errors when loading the config.
     */
    void loadConfig(@NotNull String configFile) throws ConfigIOException, ConfigInvalidException;
}
