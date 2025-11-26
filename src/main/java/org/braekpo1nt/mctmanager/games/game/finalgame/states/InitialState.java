package org.braekpo1nt.mctmanager.games.game.finalgame.states;

import org.braekpo1nt.mctmanager.games.base.states.DoNothingState;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalParticipant;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalTeam;
import org.jetbrains.annotations.NotNull;

public class InitialState implements FinalState, DoNothingState<FinalParticipant, FinalTeam> {
    @Override
    public void onOpenKitPicker(@NotNull FinalParticipant participant) {
        // do nothing
    }
}
