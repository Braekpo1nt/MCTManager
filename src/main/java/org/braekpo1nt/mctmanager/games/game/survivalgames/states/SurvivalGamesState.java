package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import org.braekpo1nt.mctmanager.games.base.states.GameStateBase;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesParticipant;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesTeam;

public interface SurvivalGamesState extends GameStateBase<SurvivalGamesParticipant, SurvivalGamesTeam> {
    void enter();
    void exit();
}
