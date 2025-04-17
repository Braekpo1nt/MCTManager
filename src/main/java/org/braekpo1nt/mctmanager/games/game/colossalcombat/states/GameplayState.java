package org.braekpo1nt.mctmanager.games.game.colossalcombat.states;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.experimental.Affiliation;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalCombatGame;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalParticipant;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalTeam;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.config.ColossalCombatConfig;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.util.BoundingBox;
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
        onParticipantDeath(participant);
    }
    
    /**
     * Only called when a non-{@link Affiliation#SPECTATOR} dies
     * @param participant a non-spectator who died
     */
    private void onParticipantDeath(@NotNull ColossalParticipant participant) {
        participant.setAlive(false);
        context.updateAliveStatus(participant.getAffiliation());
        context.addDeath(participant);
        Player killer = participant.getKiller();
        if (killer != null) {
            ColossalParticipant killerParticipant = context.getParticipants().get(killer.getUniqueId());
            if (killerParticipant != null) {
                UIUtils.showKillTitle(killerParticipant, participant);
                context.addKill(killerParticipant);
            }
        }
        if (context.getTeams().get(participant.getTeamId()).isDead()) {
            switch (participant.getAffiliation()) {
                case NORTH -> onTeamWinRound(southTeam);
                case SOUTH -> onTeamWinRound(northTeam);
            }
        }
    }
    
    /**
     * Called when a given team wins
     * @param winner the team which won
     */
    protected void onTeamWinRound(@NotNull ColossalTeam winner) {
        cleanup();
        winner.setWins(winner.getWins() + 1);
        context.updateRoundSidebar();
        if (winner.getWins() >= config.getRequiredWins()) {
            // declare overall winner
            context.messageAllParticipants(Component.empty()
                    .append(winner.getFormattedDisplayName())
                    .append(Component.text(" wins the game!")));
            context.titleAllParticipants(UIUtils.defaultTitle(
                    Component.empty()
                            .append(winner.getFormattedDisplayName()),
                    Component.empty()
                            .append(Component.text("wins"))
            ));
            EventManager eventManager = context.getGameManager().getEventManager();
            if (eventManager.colossalCombatIsActive()) {
                eventManager.setWinningTeam(winner);
            }
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
    public void onParticipantRespawn(PlayerRespawnEvent event, ColossalParticipant participant) {
        super.onParticipantRespawn(event, participant);
        if (participant.getAffiliation() == Affiliation.SPECTATOR) {
            return;
        }
        event.setRespawnLocation(participant.getLocation());
    }
    
    @Override
    public void onParticipantPostRespawn(PlayerPostRespawnEvent event, ColossalParticipant participant) {
        if (participant.getAffiliation() == Affiliation.SPECTATOR) {
            return;
        }
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
    
    @Override
    public void onParticipantRejoin(ColossalParticipant participant, ColossalTeam team) {
        super.onParticipantRejoin(participant, team);
        if (participant.getAffiliation() == Affiliation.SPECTATOR) {
            return;
        }
        participant.setAlive(false);
        participant.setGameMode(GameMode.SPECTATOR);
        context.updateAliveStatus(participant.getAffiliation());
    }
    
    @Override
    public void onNewParticipantJoin(ColossalParticipant participant, ColossalTeam team) {
        super.onNewParticipantJoin(participant, team);
        if (participant.getAffiliation() == Affiliation.SPECTATOR) {
            return;
        }
        participant.setAlive(false);
        participant.setGameMode(GameMode.SPECTATOR);
        context.updateAliveStatus(participant.getAffiliation());
    }
    
    @Override
    public void onParticipantQuit(ColossalParticipant participant, ColossalTeam team) {
        if (participant.getAffiliation() != Affiliation.SPECTATOR) {
            context.messageAllParticipants(Component.empty()
                    .append(participant.displayName())
                    .append(Component.text(" left early. Their life is forfeit.")));
            onParticipantDeath(participant);
        }
        super.onParticipantQuit(participant, team);
    }
}
