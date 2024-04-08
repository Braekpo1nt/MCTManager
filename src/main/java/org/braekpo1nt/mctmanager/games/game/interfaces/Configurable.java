package org.braekpo1nt.mctmanager.games.game.interfaces;

public interface Configurable {
    /**
     * Load the config file and assign any necessary values. If the config file fails to load, throws an IllegalArgumentException.
     * @return true if the config loaded successfully
     * @throws IllegalArgumentException if the config fails to load for any reason.
     */
    boolean loadConfig() throws IllegalArgumentException;
}
