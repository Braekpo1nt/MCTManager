package org.braekpo1nt.mctmanager.games.game.finalgame.states;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.base.Affiliation;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalGame;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalParticipant;
import org.braekpo1nt.mctmanager.games.game.finalgame.config.FinalConfig;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.braekpo1nt.mctmanager.utils.MathUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProtectionState extends FinalStateBase {
    
    private final @NotNull FinalConfig config;
    private final Map<String, SpawnIndex> kitSpawnIter;
    private @Nullable Timer timer;
    private @Nullable Map<UUID, Location> spawnLocations;
    
    public ProtectionState(@NotNull FinalGame context) {
        super(context);
        this.config = context.getConfig();
        this.kitSpawnIter = config.getKits().keySet().stream()
                .collect(Collectors.toMap(key -> key, key -> new SpawnIndex()));
    }
    
    @Override
    public void enter() {
        spawnLocations = createSpawnLocations();
        placeSpawnProtection();
        teleportParticipantsToSpawns();
        this.timer = context.getTimerManager().start(Timer.builder()
                .duration(config.getProtectionDuration())
                .withTopbar(context.getTopbar())
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Grace Period: "))
                .topbarPrefix(Component.text("Grace Period: "))
                .onCompletion(() -> {
                    removeSpawnProtection();
                    spawnLocations = null;
                    context.setState(new RoundActiveState(context));
                })
                .build());
    }
    
    @Override
    public void exit() {
        Timer.cancel(timer);
        removeSpawnProtection();
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
    
    /**
     * Spawn the participants in their arena, according to their affiliation and kit,
     * using the kit's spawn points.
     */
    private Map<UUID, Location> createSpawnLocations() {
        Map<UUID, Location> spawnLocations = new HashMap<>(context.getParticipants().size());
        // holds an iterator for each keySet
        for (FinalParticipant participant : context.getParticipants().values()) {
            String kitId = participant.getKitId();
            SpawnIndex spawnIndex = kitSpawnIter.get(kitId);
            switch (participant.getAffiliation()) {
                case NORTH -> {
                    List<Location> northSpawns = config.getKits().get(kitId).getNorthSpawns();
                    spawnLocations.put(participant.getUniqueId(), northSpawns.get(spawnIndex.getNorth()));
                    spawnIndex.incrementNorth(northSpawns.size());
                }
                case SOUTH -> {
                    List<Location> southSpawns = config.getKits().get(kitId).getSouthSpawns();
                    spawnLocations.put(participant.getUniqueId(), southSpawns.get(spawnIndex.getSouth()));
                    spawnIndex.incrementSouth(southSpawns.size());
                }
            }
        }
        return spawnLocations;
    }
    
    /**
     * Spawn the participants in their arena, according to their affiliation and kit,
     * using the kit's spawn points.
     */
    private void teleportParticipantsToSpawns() {
        if (spawnLocations == null) {
            return;
        }
        for (FinalParticipant participant : context.getParticipants().values()) {
            if (participant.getAffiliation() != Affiliation.SPECTATOR) {
                Location location = spawnLocations.get(participant.getUniqueId());
                participant.teleport(location);
            }
        }
    }
    
    private void placeSpawnProtection() {
        if (spawnLocations == null) {
            return;
        }
        for (Location location : spawnLocations.values()) {
            BlockPlacementUtils.createBarrierCube(location, Material.AIR, Material.BARRIER);
        }
    }
    
    private void removeSpawnProtection() {
        if (spawnLocations == null) {
            return;
        }
        for (Location location : spawnLocations.values()) {
            BlockPlacementUtils.createBarrierCube(location, Material.BARRIER, Material.AIR);
        }
    }
}
