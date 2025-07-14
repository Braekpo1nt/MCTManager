package org.braekpo1nt.mctmanager.games.game.clockwork.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkGame;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkParticipant;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkTeam;
import org.braekpo1nt.mctmanager.games.game.clockwork.config.ClockworkConfig;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RoundActiveState extends ClockworkStateBase {
    
    protected final @NotNull ClockworkConfig config;
    
    public RoundActiveState(@NotNull ClockworkGame context) {
        super(context);
        this.config = context.getConfig();
    }
    
    /**
     * @return the living teams
     */
    protected @NotNull List<ClockworkTeam> getLivingTeams() {
        return context.getTeams().values().stream().filter(ClockworkTeam::isAlive).toList();
    }
    
    /**
     * @param newParticipantsToKill the participants to kill (each participant will be checked for alive 
     *                              status before being killed)
     */
    protected void killParticipants(Collection<ClockworkParticipant> newParticipantsToKill) {
        Collection<ClockworkParticipant> participantsToKill = newParticipantsToKill.stream()
                .filter(ClockworkParticipant::isAlive)
                .toList();
        if (participantsToKill.isEmpty()) {
            return;
        }
        // teams which were already dead
        List<ClockworkTeam> existingDeadTeams = context.getTeams().values().stream()
                .filter(ClockworkTeam::isDead).toList();
        // participants who will be left alive once participantsToKill are killed
        List<ClockworkParticipant> survivingParticipants = context.getParticipants().values().stream()
                .filter(ClockworkParticipant::isAlive)
                .filter(p -> !participantsToKill.contains(p))
                .toList();
        
        for (ClockworkParticipant toKill : participantsToKill) {
            toKill.setGameMode(GameMode.SPECTATOR);
            toKill.getInventory().clear();
            ParticipantInitializer.clearStatusEffects(toKill);
            ParticipantInitializer.resetHealthAndHunger(toKill);
            toKill.setAlive(false);
            context.messageAllParticipants(Component.empty()
                    .append(toKill.displayName())
                    .append(Component.text(" was claimed by time")));
            String killedTeamId = toKill.getTeamId();
            
            // award living participants start
            List<ClockworkParticipant> awardedParticipants = survivingParticipants.stream()
                    .filter(p -> !p.getTeamId().equals(killedTeamId))
                    .toList();
            context.awardParticipantPoints(awardedParticipants, config.getPlayerEliminationScore());
            // award living participants end
        }
        // who are now dead, which weren't at the start of this method
        List<ClockworkTeam> newlyKilledTeams = context.getTeams().values().stream()
                .filter(t -> !existingDeadTeams.contains(t))
                .filter(ClockworkTeam::isDead)
                .toList();
        if (newlyKilledTeams.isEmpty()) {
            return;
        }
        List<ClockworkTeam> survivingTeams = context.getTeams().values().stream()
                .filter(ClockworkTeam::isAlive)
                .filter(t -> !newlyKilledTeams.contains(t))
                .toList();
        for (ClockworkTeam newlyKilledTeam : newlyKilledTeams) {
            newlyKilledTeam.sendMessage(Component.empty()
                    .append(newlyKilledTeam.getFormattedDisplayName())
                    .append(Component.text(" has been eliminated"))
                    .color(NamedTextColor.DARK_RED));
            Audience.audience(context.getTeams().values().stream().filter(p ->
                            !p.getTeamId().equals(newlyKilledTeam.getTeamId()))
                    .toList()).sendMessage(Component.empty()
                    .append(newlyKilledTeam.getFormattedDisplayName())
                    .append(Component.text(" has been eliminated"))
                    .color(NamedTextColor.GREEN));
            
            context.awardTeamPoints(survivingTeams, config.getTeamEliminationScore());
        }
    }
    
    @Override
    public void onNewParticipantJoin(ClockworkParticipant participant, ClockworkTeam team) {
        super.onNewParticipantJoin(participant, team);
        participant.setAlive(false);
        participant.setGameMode(GameMode.SPECTATOR);
        participant.teleport(context.getConfig().getStartingLocation());
    }
    
    @Override
    public void onParticipantRejoin(ClockworkParticipant participant, ClockworkTeam team) {
        super.onParticipantRejoin(participant, team);
        participant.setAlive(false);
        participant.setGameMode(GameMode.SPECTATOR);
        participant.teleport(context.getConfig().getStartingLocation());
    }
    
    @Override
    public void onParticipantQuit(ClockworkParticipant participant, ClockworkTeam team) {
        super.onParticipantQuit(participant, team);
        if (participant.isAlive()) {
            killParticipants(Collections.singletonList(participant));
        }
    }
}
