package org.braekpo1nt.mctmanager.games.game.finalgame.states;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.base.Affiliation;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalGame;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalGameKit;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalParticipant;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalTeam;
import org.braekpo1nt.mctmanager.games.game.finalgame.config.FinalConfig;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.braekpo1nt.mctmanager.utils.MathUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
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
    
    @Override
    public void onParticipantDeath(@NotNull PlayerDeathEvent event, @NotNull FinalParticipant participant) {
        if (participant.getAffiliation() == Affiliation.SPECTATOR) {
            return;
        }
        event.getDrops().clear();
        event.setDroppedExp(0);
    }
    
    @Override
    public void onParticipantRespawn(@NotNull PlayerRespawnEvent event, @NotNull FinalParticipant participant) {
        switch (participant.getAffiliation()) {
            case NORTH, SOUTH -> event.setRespawnLocation(participant.getLocation());
            case SPECTATOR -> event.setRespawnLocation(context.getConfig().getSpectatorSpawn());
        }
    }
    
    @Override
    public void onParticipantPostRespawn(@Nullable PlayerPostRespawnEvent event, @NotNull FinalParticipant participant) {
        super.onParticipantPostRespawn(event, participant);
        giveKitItems(participant);
    }
    
    private void giveKitItems(@NotNull FinalParticipant participant) {
        FinalTeam team = context.getTeams().get(participant.getTeamId());
        FinalGameKit kit = config.getKits().get(participant.getKitId());
        if (kit.isHasBanner()) {
            participant.getInventory().setHelmet(new ItemStack(team.getColorAttributes().getBanner()));
        }
        ColorMap.colorLeatherArmor(participant, team.getBukkitColor());
    }
    
    @Override
    public void onParticipantRejoin(FinalParticipant participant, FinalTeam team) {
        super.onParticipantRejoin(participant, team);
        if (participant.getAffiliation() == Affiliation.SPECTATOR) {
            return;
        }
        participant.setAlive(false);
        context.getTabList().setParticipantGrey(participant, true);
        participant.setGameMode(GameMode.SPECTATOR);
        context.updateAliveStatus(participant.getAffiliation());
    }
    
    @Override
    public void onNewParticipantJoin(FinalParticipant participant, FinalTeam team) {
        super.onNewParticipantJoin(participant, team);
        if (participant.getAffiliation() == Affiliation.SPECTATOR) {
            return;
        }
        participant.setAlive(false);
        context.getTabList().setParticipantGrey(participant, true);
        participant.setGameMode(GameMode.SPECTATOR);
        context.updateAliveStatus(participant.getAffiliation());
    }
    
    @Override
    public void onParticipantQuit(FinalParticipant participant, FinalTeam team) {
        if (participant.isAlive() && participant.getAffiliation() != Affiliation.SPECTATOR) {
            context.messageAllParticipants(Component.empty()
                    .append(participant.displayName())
                    .append(Component.text(" left early. Their life is forfeit.")));
        }
        super.onParticipantQuit(participant, team);
    }
}
