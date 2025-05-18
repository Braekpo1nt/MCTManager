package org.braekpo1nt.mctmanager.games.game.interfaces;

import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface GameEditor extends Configurable {
    void start(Collection<Player> newParticipants);
    void stop();
    
    GameType getType();
    
    boolean configIsValid(@NotNull String configFile);
    
    void saveConfig(@NotNull String configFile) throws ConfigIOException, ConfigInvalidException;
}
