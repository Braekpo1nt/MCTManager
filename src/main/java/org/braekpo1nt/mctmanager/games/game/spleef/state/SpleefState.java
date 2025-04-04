package org.braekpo1nt.mctmanager.games.game.spleef.state;

import org.braekpo1nt.mctmanager.games.experimental.GameStateBase;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefParticipant;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefTeam;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public interface SpleefState extends GameStateBase<SpleefParticipant, SpleefTeam> {
    
    void onParticipantBreakBlock(BlockBreakEvent event, SpleefParticipant participant);
    
    void onParticipantRespawn(PlayerRespawnEvent event, SpleefParticipant participant);
}
