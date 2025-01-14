package org.braekpo1nt.mctmanager.games.game.interfaces;

import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.participant.Participant;

import java.util.Collection;

public interface GameEditor extends Configurable {
    void start(Collection<Participant> newParticipants);
    void stop();
    
    GameType getType();
    
    boolean configIsValid();
    
    void saveConfig() throws ConfigIOException, ConfigInvalidException;
}
