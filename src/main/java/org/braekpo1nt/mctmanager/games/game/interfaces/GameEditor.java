package org.braekpo1nt.mctmanager.games.game.interfaces;

import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.bukkit.entity.Player;

import java.util.List;

public interface GameEditor extends Configurable {
    void start(List<Player> newParticipants);
    void stop();
    
    GameType getType();
    
    boolean configIsValid();
    
    void saveConfig() throws ConfigIOException, ConfigInvalidException;
}
