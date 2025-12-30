package org.braekpo1nt.mctmanager.games.game.finalgame.states;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.braekpo1nt.mctmanager.games.base.Affiliation;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalGame;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalParticipant;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RoundActiveState extends FinalStateBase {
    
    public RoundActiveState(@NotNull FinalGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        // holds an iterator for each keySet
        Map<String, Integer> kitSpawnIter = context.getConfig().getKits().keySet().stream()
                .collect(Collectors.toMap(key -> key, key -> 0));
        for (FinalParticipant participant : context.getParticipants().values()) {
            switch (participant.getAffiliation()) {
                case NORTH -> {
                    kitSpawnIter.get(participant.getKitId());
                }
                case SOUTH -> {
                    
                }
            }
        }
        // kick off ammo refill timer (each kit has different delays, but they're all in 
        //   increments of seconds, so it can be one timer with a single tracker for each kit type)
        // kick off lava rise timer
    }
    
    @Override
    public void exit() {
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
