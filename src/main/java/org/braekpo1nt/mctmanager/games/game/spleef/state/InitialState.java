package org.braekpo1nt.mctmanager.games.game.spleef.state;

import org.braekpo1nt.mctmanager.games.base.states.DoNothingState;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefParticipant;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefTeam;
import org.bukkit.event.block.BlockBreakEvent;

public class InitialState implements SpleefState, DoNothingState<SpleefParticipant, SpleefTeam> {
    @Override
    public void onParticipantBreakBlock(BlockBreakEvent event, SpleefParticipant participant) {
        
    }
}
