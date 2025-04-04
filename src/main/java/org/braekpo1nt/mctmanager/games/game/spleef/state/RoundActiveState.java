package org.braekpo1nt.mctmanager.games.game.spleef.state;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.spleef.*;
import org.braekpo1nt.mctmanager.games.game.spleef.powerup.PowerupManager;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.GameMode;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class RoundActiveState extends SpleefStateBase implements SpleefInterface {
    
    private final DecayManager decayManager;
    private final PowerupManager powerupManager;
    
    public RoundActiveState(@NotNull SpleefGame context) {
        super(context);
        this.decayManager = new DecayManager(context.getPlugin(), context.getConfig(), this);
        this.powerupManager = new PowerupManager(context.getPlugin(), context.getConfig());
        for (SpleefParticipant participant : context.getParticipants().values()) {
            giveTool(participant);
            participant.setAlive(true);
            participant.setGameMode(GameMode.SURVIVAL);
        }
        decayManager.setAliveCount(context.getParticipants().size());
        decayManager.setAlivePercent(1.0);
        decayManager.start();
        powerupManager.start(context.getParticipants().values());
    }
    
    @Override
    public void cleanup() {
        decayManager.stop();
        powerupManager.stop();
    }
    
    private void stop() {
        cleanup();
        context.setState(new RoundOverState(context));
    }
    
    /**
     * Give the Spleef tool to the participant
     * @param participant the participant
     */
    private void giveTool(SpleefParticipant participant) {
        participant.getInventory().addItem(context.getConfig().getTool());
    }
    
    @Override
    public void onParticipantRejoin(SpleefParticipant participant, SpleefTeam team) {
        super.onParticipantRejoin(participant, team);
        participant.setAlive(false);
        participant.setGameMode(GameMode.SPECTATOR);
        updateAliveCount();
    }
    
    @Override
    public void onNewParticipantJoin(SpleefParticipant participant, SpleefTeam team) {
        super.onNewParticipantJoin(participant, team);
        participant.setAlive(true);
        participant.setGameMode(GameMode.SURVIVAL);
        giveTool(participant);
        powerupManager.addParticipant(participant);
        updateAliveCount();
    }
    
    @Override
    public void onParticipantQuit(SpleefParticipant participant, SpleefTeam team) {
        if (participant.isAlive()) {
            Component deathMessage = Component.empty()
                    .append(Component.text(participant.getName()))
                    .append(Component.text(" left early. Their life is forfeit."));
            PlayerDeathEvent fakeDeathEvent = new PlayerDeathEvent(participant.getPlayer(),
                    DamageSource.builder(DamageType.GENERIC).build(), Collections.emptyList(), 0, deathMessage);
            onParticipantDeath(fakeDeathEvent, participant);
        }
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull SpleefParticipant participant) {
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (!cause.equals(EntityDamageEvent.DamageCause.LAVA)
                && !cause.equals(EntityDamageEvent.DamageCause.FIRE)) {
            Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "SpleefRound.onPlayerDamage()->not fire or lava cancelled");
            event.setCancelled(true);
        }
    }
    
    @Override
    public void onParticipantDeath(@NotNull PlayerDeathEvent event, @NotNull SpleefParticipant killed) {
        if (!killed.isAlive()) {
            return;
        }
        killed.setGameMode(GameMode.SPECTATOR);
        Main.debugLog(LogType.CANCEL_PLAYER_DEATH_EVENT, "SpleefRound.onPlayerDeath() cancelled");
        event.setCancelled(true);
        Component deathMessage = event.deathMessage();
        if (deathMessage != null) {
            context.getPlugin().getServer().sendMessage(deathMessage);
        }
        onParticipantDeath(killed);
        if (lessThanTwoParticipantsAreAlive() || exactlyOneTeamIsAlive()) {
            stop();
        }
    }
    
    /**
     * @return true if exactly one team is alive, false otherwise
     */
    private boolean exactlyOneTeamIsAlive() {
        return context.getTeams().values().stream().filter(SpleefTeam::isAlive).count() == 1;
    }
    
    /**
     * @return true if fewer than 2 participants are alive
     */
    private boolean lessThanTwoParticipantsAreAlive() {
        return getAliveCount() < 2;
    }
    
    /**
     * @return the number of participants who are alive in this round
     */
    private long getAliveCount() {
        return context.getParticipants().values().stream().filter(SpleefParticipant::isAlive).count();
    }
    
    private void updateAliveCount() {
        long aliveCount = getAliveCount();
        Component alive = Component.empty()
                .append(Component.text("Alive: "))
                .append(Component.text(aliveCount));
        context.getSidebar().updateLine("alive", alive);
        context.getAdminSidebar().updateLine("alive", alive);
        decayManager.setAliveCount(aliveCount);
        decayManager.setAlivePercent(aliveCount / (double) context.getParticipants().size());
    }
    
    private void onParticipantDeath(SpleefParticipant killed) {
        context.teleportToRandomStartingPosition(killed);
        ParticipantInitializer.clearStatusEffects(killed);
        ParticipantInitializer.resetHealthAndHunger(killed);
        killed.getInventory().clear();
        killed.setAlive(false);
        powerupManager.removeParticipant(killed);
        List<SpleefParticipant> awardedParticipants = context.getParticipants().values().stream()
                .filter(SpleefParticipant::isAlive)
                .filter(participant -> !participant.getTeamId().equals(killed.getTeamId()))
                .toList();
        context.awardPoints(awardedParticipants, context.getConfig().getSurviveScore());
        
        updateAliveCount();
    }
    
    @Override
    public void onParticipantInteract(@NotNull PlayerInteractEvent event, @NotNull SpleefParticipant participant) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setUseInteractedBlock(Event.Result.DENY);
        }
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull SpleefParticipant participant) {
        // do nothing
    }
    
    @Override
    public void onParticipantBreakBlock(BlockBreakEvent event, SpleefParticipant participant) {
        powerupManager.onParticipantBreakBlock(participant);
        event.setDropItems(false);
    }
    
    @Override
    public void messageAllParticipants(@NotNull Component message) {
        context.messageAllParticipants(message);
    }
    
    @Override
    public void titleAllParticipants(@NotNull Title title) {
        context.titleAllParticipants(title);
    }
    
    @Override
    public void setShouldGivePowerups(boolean shouldGivePowerups) {
        context.setShouldGivePowerups(shouldGivePowerups);
    }
}
