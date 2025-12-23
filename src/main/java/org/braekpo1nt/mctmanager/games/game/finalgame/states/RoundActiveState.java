package org.braekpo1nt.mctmanager.games.game.finalgame.states;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalGame;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalParticipant;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RoundActiveState extends FinalStateBase {
    
    public RoundActiveState(@NotNull FinalGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        // spawn in players
        // kick off ammo refill timer (each kit has different delays, but they're all in 
        //   increments of seconds, so it can be one timer with a single tracker for each kit type)
        // kick off lava rise timer
    }
    
    @Override
    public void exit() {
        // clean the items on the ground
        // 
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull FinalParticipant participant) {
        // if a participant is not allowed to melee, prevent it
    }
    
    @Override
    public void onParticipantPostRespawn(@Nullable PlayerPostRespawnEvent event, @NotNull FinalParticipant participant) {
        super.onParticipantPostRespawn(event, participant);
        // first, check win condition (are all players dead?) if so, end the round
        
        // increment the number of dead players for the lava rise
        // trigger lava rise if threshold reached, and reset count
    }
}
