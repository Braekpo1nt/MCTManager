package org.braekpo1nt.mctmanager.games.game.finalgame.states;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalGame;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalGameKit;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalParticipant;
import org.braekpo1nt.mctmanager.games.game.finalgame.config.FinalConfig;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.braekpo1nt.mctmanager.utils.MathUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RoundActiveState extends FinalStateBase {
    
    private final @NotNull FinalConfig config;
    private int refillTaskId;
    private @Nullable Timer lavaTimer;
    private int lavaDeaths;
    /**
     * The current y level that the lava is at
     */
    private int lavaYLevel;
    
    public RoundActiveState(@NotNull FinalGame context) {
        super(context);
        this.config = context.getConfig();
        this.lavaDeaths = 0;
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
        spawnParticipantsInArena();
        // kick off ammo refill timer (each kit has different delays, but they're all in 
        //   increments of seconds, so it can be one timer with a single tracker for each kit type)
        kickOffRefillTimer();
        // kick off lava rise timer
        resetLavaTimer();
    }
    
    private void resetLavaTimer() {
        Timer.cancel(this.lavaTimer);
        this.lavaTimer = context.getTimerManager().start(Timer.builder()
                .duration(config.getLava().getRiseSeconds())
                .withTopbar(context.getTopbar())
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Lava Rises: "))
                .topbarPrefix(Component.text("Lava Rises: "))
                .onCompletion(this::raiseTheLavaLevel)
                .build());
    }
    
    private void raiseTheLavaLevel() {
        if (lavaIsAtMaxY()) {
            return;
        }
        BoundingBox lavaArea = config.getLava().getLavaArea();
        int bottomYLevel = lavaYLevel + 1;
        lavaYLevel = Math.min((int) lavaArea.getMaxY(), lavaYLevel + config.getLava().getBlocksPerRise());
        BlockPlacementUtils.createCubeReplace(
                config.getWorld(),
                new BoundingBox(
                        lavaArea.getMinX(),
                        bottomYLevel,
                        lavaArea.getMinZ(),
                        lavaArea.getMaxX(),
                        lavaYLevel,
                        lavaArea.getMaxZ()
                ),
                Material.AIR,
                Material.LAVA
        );
        if (lavaIsAtMaxY()) {
            Timer.cancel(lavaTimer);
            return;
        }
        resetLavaTimer();
    }
    
    private boolean lavaIsAtMaxY() {
        return lavaYLevel >= config.getLava().getLavaArea().getMaxY();
    }
    
    private void kickOffRefillTimer() {
        refillTaskId = new BukkitRunnable() {
            final Map<String, Integer> refillCountdowns = config.getKits().entrySet().stream()
                    // no refills given if refillSeconds is less than 1
                    .filter(entry -> entry.getValue().hasRefills())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().getRefillSeconds()));
            
            
            @Override
            public void run() {
                for (Map.Entry<String, Integer> entry : refillCountdowns.entrySet()) {
                    entry.setValue(runForKit(entry.getKey(), entry.getValue()));
                }
            }
            
            /**
             * The countdown logic for each kit.
             * @param kitId the unique id of the kit to run the countdown logic for
             * @param countdown how many seconds are left in the countdown for this kit
             * @return the decremented countdown seconds, or the reset countdown, depending on the value of the given countdown seconds
             */
            private int runForKit(String kitId, int countdown) {
                if (countdown <= 0) {
                    FinalGameKit kit = config.getKits().get(kitId);
                    giveRefills(kitId, kit);
                    // reset the counter for this kit
                    return kit.getRefillSeconds();
                }
                return countdown + 1;
            }
        }.runTaskTimer(context.getPlugin(), 0L, 20L).getTaskId();
    }
    
    /**
     * Give refills for the given kit to all participants with the kit
     * @param kitId the id of the kit
     * @param kit the kit to give the refills of
     */
    private void giveRefills(String kitId, FinalGameKit kit) {
        for (FinalParticipant participant : context.getParticipants().values()) {
            if (participant.hasKit(kitId)) {
                kit.refill(participant);
            }
        }
    }
    
    /**
     * Spawn the participants in their arena, according to their affiliation and kit,
     * using the kit's spawn points.
     */
    private void spawnParticipantsInArena() {
        // holds an iterator for each keySet
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
        context.getPlugin().getServer().getScheduler().cancelTask(refillTaskId);
        Timer.cancel(lavaTimer);
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull FinalParticipant participant) {
        Entity causingEntity = event.getDamageSource().getCausingEntity();
        switch (causingEntity) {
            case Player player -> {
                FinalParticipant causingParticipant = context.getParticipants().get(player.getUniqueId());
                if (causingParticipant == null) {
                    return;
                }
                if (causingParticipant.getKitId() == null) {
                    return;
                }
                if (config.getKits().get(causingParticipant.getKitId()).isMelee()) {
                    return;
                }
                // if a participant is not allowed to melee, prevent damage but not knockback
                event.setDamage(0);
            }
            case Arrow ignored -> event.setDamage(event.getDamage() * config.getArrowDamageModifier());
            case null, default -> {
            }
        }
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
        // mark this participant as dead and set to spectator
        participant.setAlive(false);
        participant.setGameMode(GameMode.SPECTATOR);
        
        // Check win condition (are all members of the dead participant's team dead?) if so, end the round and return
        if (context.getTeams().get(participant.getTeamId()).isAlive()) {
            context.setState(new RoundOverState(context));
            return;
        }
        this.lavaDeaths++;
        if (lavaDeaths >= config.getLava().getRiseDeaths()) {
            // reset the counter
            lavaDeaths = 0;
            raiseTheLavaLevel();
        }
    }
}
