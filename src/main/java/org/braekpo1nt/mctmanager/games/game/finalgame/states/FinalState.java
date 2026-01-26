package org.braekpo1nt.mctmanager.games.game.finalgame.states;

import org.braekpo1nt.mctmanager.games.base.states.GameStateBase;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalParticipant;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalTeam;
import org.jetbrains.annotations.NotNull;

public interface FinalState extends GameStateBase<FinalParticipant, FinalTeam> {
    /**
     * Called when a given participant uses the wand to show the kit picker
     * @param participant the participant who opened the class picker
     */
    void onOpenKitPicker(@NotNull FinalParticipant participant);
}
