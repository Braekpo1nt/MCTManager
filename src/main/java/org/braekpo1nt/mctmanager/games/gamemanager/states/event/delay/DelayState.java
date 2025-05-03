package org.braekpo1nt.mctmanager.games.gamemanager.states.event.delay;

import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.states.ContextReference;
import org.braekpo1nt.mctmanager.games.gamemanager.states.event.EventData;
import org.braekpo1nt.mctmanager.games.gamemanager.states.event.EventState;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public abstract class DelayState extends EventState {
    public DelayState(@NotNull GameManager context, @NotNull ContextReference contextReference, @NotNull EventData eventData) {
        super(context, contextReference, eventData);
    }
    
    @Override
    public CommandResult startGame(Set<String> teamIds, @NotNull GameType gameType, @NotNull String configFile) {
        return CommandResult.failure("Can't start a game during a delay state");
    }
}
