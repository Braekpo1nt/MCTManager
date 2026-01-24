package org.braekpo1nt.mctmanager.games.game.finalgame.states;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.base.Affiliation;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalGame;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalGameKit;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalParticipant;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalTeam;
import org.braekpo1nt.mctmanager.games.game.finalgame.config.FinalConfig;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        this.lavaYLevel = ((int) config.getLava().getLavaArea().getMinY());
    }
    
    @Override
    public void enter() {
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
        context.titleAllParticipants(UIUtils.defaultSubtitle(Component.empty()
                .append(Component.text("Lava Rises"))
                .color(NamedTextColor.GOLD)));
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
            context.getTopbar().setMiddle(Component.empty()
                    .append(Component.text("Sudden Death"))
                    .color(NamedTextColor.RED));
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
                return countdown - 1;
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
            if (participant.isAlive() && participant.hasKit(kitId)) {
                kit.refill(participant);
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
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (!cause.equals(EntityDamageEvent.DamageCause.LAVA)
                && !cause.equals(EntityDamageEvent.DamageCause.FIRE)
                && !cause.equals(EntityDamageEvent.DamageCause.FIRE_TICK)) {
            event.setDamage(0);
        }
        if (participant.getAffiliation() == Affiliation.SPECTATOR) {
            event.setCancelled(true);
            return;
        }
        Entity directEntity = event.getDamageSource().getDirectEntity();
        if (directEntity instanceof Arrow) {
            event.setDamage(event.getDamage() * config.getArrowDamageModifier());
            return;
        }
        if (!(directEntity instanceof Player player)) {
            return;
        }
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
        event.setCancelled(true);
    }
    
    @Override
    public void onParticipantDeath(@NotNull PlayerDeathEvent event, @NotNull FinalParticipant participant) {
        if (participant.getAffiliation() == Affiliation.SPECTATOR) {
            return;
        }
        event.getDrops().clear();
        event.setDroppedExp(0);
        Player killer = participant.getKiller();
        if (killer != null) {
            FinalParticipant killerParticipant = context.getParticipants().get(killer.getUniqueId());
            if (killerParticipant != null) {
                UIUtils.showKillTitle(killerParticipant, participant);
                context.addKill(killerParticipant);
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
        onParticipantDeath(participant);
    }
    
    /**
     * Some common functionality for when a participant dies, or quits
     * @param participant the participant who died
     */
    private void onParticipantDeath(FinalParticipant participant) {
        participant.setAlive(false);
        participant.setGameMode(GameMode.SPECTATOR);
        context.getTabList().setParticipantGrey(participant, true);
        context.updateAliveStatus(participant.getAffiliation());
        context.addDeath(participant);
        
        // Check win condition (are all members of the dead participant's team dead?) if so, end the round and return
        if (context.getTeams().get(participant.getTeamId()).isDead()) {
            switch (participant.getAffiliation()) {
                case NORTH -> onTeamWinRound(context.getSouthTeam());
                case SOUTH -> onTeamWinRound(context.getNorthTeam());
            }
            return;
        }
        this.lavaDeaths++;
        if (lavaDeaths >= config.getLava().getRiseDeaths()) {
            // reset the counter
            lavaDeaths = 0;
            raiseTheLavaLevel();
        }
    }
    
    private void onTeamWinRound(@NotNull FinalTeam winner) {
        winner.setWins(winner.getWins() + 1);
        context.updateRoundSidebar();
        
        context.getGameManager().setWinner(winner.getTeamId());
        if (winner.getWins() >= config.getRequiredWins()) {
            // declare overall winner
            context.messageAllParticipants(Component.empty()
                    .append(winner.getFormattedDisplayName())
                    .append(Component.text(" wins the game!")));
            context.titleAllParticipants(UIUtils.defaultTitle(
                    Component.empty()
                            .append(Component.text("Winner:")),
                    Component.empty()
                            .append(winner.getFormattedDisplayName())
            ));
            context.setState(new GameOverState(context));
        } else {
            // declare winner of round
            context.messageAllParticipants(Component.empty()
                    .append(winner.getFormattedDisplayName())
                    .append(Component.text(" won this round!")));
            context.setState(new RoundOverState(context));
        }
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
            onParticipantDeath(participant);
        }
        super.onParticipantQuit(participant, team);
    }
}
