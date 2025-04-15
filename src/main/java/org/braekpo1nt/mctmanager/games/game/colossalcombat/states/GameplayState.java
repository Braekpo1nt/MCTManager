package org.braekpo1nt.mctmanager.games.game.colossalcombat.states;

import org.braekpo1nt.mctmanager.games.experimental.Affiliation;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalCombatGame;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalParticipant;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalTeam;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.config.ColossalCombatConfig;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;

public abstract class GameplayState extends ColossalCombatStateBase {
    
    protected final @NotNull ColossalCombatConfig config;
    protected final @NotNull ColossalTeam northTeam;
    protected final @NotNull ColossalTeam southTeam;
    
    public GameplayState(@NotNull ColossalCombatGame context) {
        super(context);
        this.config = context.getConfig();
        this.northTeam = context.getNorthTeam();
        this.southTeam = context.getSouthTeam();
    }
    
    @Override
    public void onParticipantDeath(@NotNull PlayerDeathEvent event, @NotNull ColossalParticipant participant) {
        if (participant.getAffiliation() == Affiliation.SPECTATOR) {
            return;
        }
        event.getDrops().clear();
        event.setDroppedExp(0);
        participant.setAlive(false);
        context.addDeath(participant);
        Player killer = participant.getKiller();
        if (killer == null) {
            return;
        }
        ColossalParticipant killerParticipant = context.getParticipants().get(killer.getUniqueId());
        if (killerParticipant == null) {
            return;
        }
        context.addKill(killerParticipant);
        context.updateAliveStatus(participant.getAffiliation());
        if (context.getTeams().get(participant.getTeamId()).isDead()) {
            
        }
    }
    
    /**
     * Called when a given team wins
     * @param winner the team which won
     */
    protected abstract void onTeamWin(ColossalTeam winner);
    
    @Override
    public void onParticipantRespawn(PlayerRespawnEvent event, ColossalParticipant participant) {
        super.onParticipantRespawn(event, participant);
        if (participant.getAffiliation() == Affiliation.SPECTATOR) {
            return;
        }
        event.setRespawnLocation(participant.getLocation());
        participant.setGameMode(GameMode.SPECTATOR);
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.clearInventory(participant);
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull ColossalParticipant participant) {
        if (participant.getAffiliation() == Affiliation.SPECTATOR) {
            event.setCancelled(true);
        }
    }
}
