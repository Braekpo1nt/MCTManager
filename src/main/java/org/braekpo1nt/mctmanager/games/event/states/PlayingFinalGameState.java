package org.braekpo1nt.mctmanager.games.event.states;

import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.event.states.delay.ToPodiumDelayState;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.participant.Team;
import org.jetbrains.annotations.NotNull;

public class PlayingFinalGameState extends PlayingGameState {
    
    public PlayingFinalGameState(EventManager context, @NotNull String configFile) {
        super(context);
        startGame(GameType.FINAL, configFile);
    }
    
    @Override
    public void gameIsOver(@NotNull GameType finishedGameType) {
        context.initializeParticipantsAndAdmins();
        context.setState(new ToPodiumDelayState(context));
        context.updateTeamScores();
        context.updatePersonalScores();
    }
    
}
