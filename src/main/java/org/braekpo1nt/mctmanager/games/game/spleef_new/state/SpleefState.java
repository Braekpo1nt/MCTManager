package org.braekpo1nt.mctmanager.games.game.spleef_new.state;

import org.braekpo1nt.mctmanager.games.experimental.GameStateBase;
import org.braekpo1nt.mctmanager.games.game.spleef_new.SpleefParticipant;
import org.braekpo1nt.mctmanager.games.game.spleef_new.SpleefTeam;
import org.bukkit.event.block.BlockBreakEvent;

public interface SpleefState extends GameStateBase<SpleefParticipant, SpleefTeam> {
    
    void onParticipantBreakBlock(BlockBreakEvent event, SpleefParticipant participant);
}
