package org.braekpo1nt.mctmanager.games.game.spleef.state;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.spleef.DecayManager;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefGame;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefInterface;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefParticipant;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefTeam;
import org.braekpo1nt.mctmanager.games.game.spleef.powerup.PowerupManager;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RoundActiveState extends SpleefStateBase implements SpleefInterface {
    
    private final DecayManager decayManager;
    private final PowerupManager powerupManager;
    
    public RoundActiveState(@NotNull SpleefGame context) {
        super(context);
        this.decayManager = new DecayManager(context.getPlugin(), context.getConfig(), this);
        this.powerupManager = new PowerupManager(context.getPlugin(), context.getConfig(), this);
    }
    
    @Override
    public void enter() {
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
    public void exit() {
        // do nothing
    }
    
    @Override
    public void cleanup() {
        decayManager.stop();
        powerupManager.stop();
    }
    
    private void stop() {
        cleanup();
        context.getSidebar().updateLine("alive", Component.empty());
        context.getAdminSidebar().updateLine("alive", Component.empty());
        context.setState(new RoundOverState(context));
    }
    
    /**
     * Give the Spleef tool to the participant
     * @param participant the participant
     */
    private void giveTool(SpleefParticipant participant) {
        participant.getInventory().addItem(context.getConfig().getTool());
    }
    
    public void onBlockBroken(@NotNull Block block) {
        decayManager.onBlockBroken(block);
    }
    
    @Override
    public void onParticipantRejoin(SpleefParticipant participant, SpleefTeam team) {
        super.onParticipantRejoin(participant, team);
        participant.setAlive(false);
        participant.setGameMode(GameMode.SPECTATOR);
        updateAliveCount(getAliveCount());
    }
    
    @Override
    public void onNewParticipantJoin(SpleefParticipant participant, SpleefTeam team) {
        super.onNewParticipantJoin(participant, team);
        participant.setAlive(true);
        participant.setGameMode(GameMode.SURVIVAL);
        giveTool(participant);
        powerupManager.addParticipant(participant);
        updateAliveCount(getAliveCount());
    }
    
    @Override
    public void onParticipantQuit(SpleefParticipant participant, SpleefTeam team) {
        if (participant.isAlive()) {
            Component deathMessage = Component.empty()
                    .append(participant.displayName())
                    .append(Component.text(" left early. Their life is forfeit."));
            context.simulateDeath(participant, deathMessage);
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
    public void onParticipantDeath(@NotNull PlayerDeathEvent event, @NotNull SpleefParticipant participant) {
        if (!participant.isAlive()) {
            return;
        }
        event.getDrops().clear();
    }
    
    @Override
    public void onParticipantPostRespawn(@Nullable PlayerPostRespawnEvent event, @NotNull SpleefParticipant participant) {
        super.onParticipantPostRespawn(event, participant);
        onParticipantDeath(participant);
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
    
    @Override
    protected void updateAliveCount(long aliveCount) {
        super.updateAliveCount(aliveCount);
        decayManager.setAliveCount(aliveCount);
        decayManager.setAlivePercent(aliveCount / (double) context.getParticipants().size());
    }
    
    private void onParticipantDeath(SpleefParticipant killed) {
        killed.setAlive(false);
        powerupManager.removeParticipant(killed);
        List<SpleefParticipant> awardedParticipants = context.getParticipants().values().stream()
                .filter(participant -> participant.isAlive() && !participant.sameTeam(killed))
                .toList();
        context.awardParticipantPoints(awardedParticipants, context.getConfig().getSurviveScore());
        
        updateAliveCount(getAliveCount());
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
    
    /**
     * Allows decay stages to determine whether powerups should be given while they are active.
     * @param shouldGivePowerups true if powerups should be given, false otherwise
     */
    @Override
    public void setShouldGivePowerups(boolean shouldGivePowerups) {
        powerupManager.setShouldGivePowerups(shouldGivePowerups);
    }
}
