package org.braekpo1nt.mctmanager.games.game.spleef.state;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefGame;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefParticipant;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefTeam;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.GameMode;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RoundActiveState extends SpleefStateBase {
    public RoundActiveState(@NotNull SpleefGame context) {
        super(context);
        for (SpleefParticipant participant : context.getParticipants().values()) {
            giveTool(participant);
            participant.setAlive(true);
            participant.setGameMode(GameMode.SURVIVAL);
        }
        context.getDecayManager().setAliveCount(context.getParticipants().size());
        context.getDecayManager().setAlivePercent(1.0);
    }
    
    @Override
    public void cleanup() {
        context.getDecayManager().stop();
        context.getPowerupManager().stop();
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
    
    private void onParticipantDeath(SpleefParticipant killed) {
        ParticipantInitializer.clearStatusEffects(killed);
        ParticipantInitializer.resetHealthAndHunger(killed);
        killed.getInventory().clear();
        killed.setAlive(false);
        context.getPowerupManager().removeParticipant(killed);
        List<SpleefParticipant> awardedParticipants = context.getParticipants().values().stream()
                .filter(SpleefParticipant::isAlive)
                .filter(participant -> !participant.getTeamId().equals(killed.getTeamId()))
                .toList();
        context.awardPoints(awardedParticipants, context.getConfig().getSurviveScore());
        
        long aliveCount = getAliveCount();
        Component alive = Component.empty()
                .append(Component.text("Alive: "))
                .append(Component.text(aliveCount));
        context.getSidebar().updateLine("alive", alive);
        context.getAdminSidebar().updateLine("alive", alive);
        context.getDecayManager().setAliveCount(aliveCount);
        context.getDecayManager().setAlivePercent(aliveCount / (double) context.getParticipants().size());
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
        context.getPowerupManager().onParticipantBreakBlock(participant);
        event.setDropItems(false);
    }
}
