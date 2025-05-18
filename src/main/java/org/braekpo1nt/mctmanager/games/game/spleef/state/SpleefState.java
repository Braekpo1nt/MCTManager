package org.braekpo1nt.mctmanager.games.game.spleef.state;

import org.braekpo1nt.mctmanager.games.base.states.GameStateBase;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefParticipant;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefTeam;
import org.bukkit.event.block.BlockBreakEvent;

public interface SpleefState extends GameStateBase<SpleefParticipant, SpleefTeam> {
    
    void onParticipantBreakBlock(BlockBreakEvent event, SpleefParticipant participant);
}
