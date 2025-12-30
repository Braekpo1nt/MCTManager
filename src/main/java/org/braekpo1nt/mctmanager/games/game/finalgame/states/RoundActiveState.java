package org.braekpo1nt.mctmanager.games.game.finalgame.states;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.braekpo1nt.mctmanager.games.base.Affiliation;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalGame;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalParticipant;
import org.braekpo1nt.mctmanager.games.game.finalgame.config.FinalConfig;
import org.braekpo1nt.mctmanager.utils.MathUtils;
import org.bukkit.Location;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RoundActiveState extends FinalStateBase {
    
    private final @NotNull FinalConfig config;
    
    public RoundActiveState(@NotNull FinalGame context) {
        super(context);
        this.config = context.getConfig();
    }
    
    @Getter
    @NoArgsConstructor
    @Setter
    private static class SpawnIndex {
        private int north;
        private int south;
        
        public void incrementNorth(int size) {
            north = MathUtils.wrapIndex(north + 1, size);
        }
        
        public void incrementSouth(int size) {
            south = MathUtils.wrapIndex(south + 1, size);
        }
    }
    
    @Override
    public void enter() {
        // holds an iterator for each keySet
        spawnParticipantsInArena();
        // kick off ammo refill timer (each kit has different delays, but they're all in 
        //   increments of seconds, so it can be one timer with a single tracker for each kit type)
        // kick off lava rise timer
    }
    
    /**
     * Spawn the participants in their arena, according to their affiliation and kit,
     * using the kit's spawn points.
     */
    private void spawnParticipantsInArena() {
        Map<String, SpawnIndex> kitSpawnIter = config.getKits().keySet().stream()
                .collect(Collectors.toMap(key -> key, key -> new SpawnIndex()));
        for (FinalParticipant participant : context.getParticipants().values()) {
            String kitId = participant.getKitId();
            SpawnIndex spawnIndex = kitSpawnIter.get(kitId);
            switch (participant.getAffiliation()) {
                case NORTH -> {
                    List<Location> northSpawns = config.getKits().get(kitId).getNorthSpawns();
                    participant.teleport(northSpawns.get(spawnIndex.getNorth()));
                    spawnIndex.incrementNorth(northSpawns.size());
                }
                case SOUTH -> {
                    List<Location> southSpawns = config.getKits().get(kitId).getSouthSpawns();
                    participant.teleport(southSpawns.get(spawnIndex.getSouth()));
                    spawnIndex.incrementSouth(southSpawns.size());
                }
            }
        }
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
