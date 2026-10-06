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
import java.util.concurrent.CompletableFuture;

public abstract class RoundActiveState extends ClockworkStateBase {
    
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
     * status before being killed)
     * @return a completable future with any database operations for points awarded
     */
    protected CompletableFuture<Void> killParticipants(Collection<ClockworkParticipant> newParticipantsToKill) {
        Collection<ClockworkParticipant> participantsToKill = newParticipantsToKill.stream()
                .filter(ClockworkParticipant::isAlive)
                .toList();
        if (participantsToKill.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        // teams which were already dead
        List<ClockworkTeam> existingDeadTeams = context.getTeams().values().stream()
                .filter(ClockworkTeam::isDead).toList();
        // participants who will be left alive once participantsToKill are killed
        List<ClockworkParticipant> survivingParticipants = context.getParticipants().values().stream()
                .filter(ClockworkParticipant::isAlive)
                .filter(p -> !participantsToKill.contains(p))
                .toList();
        
        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
        for (ClockworkParticipant toKill : participantsToKill) {
            toKill.setGameMode(GameMode.SPECTATOR);
            toKill.getInventory().clear();
            ParticipantInitializer.clearStatusEffects(toKill);
            ParticipantInitializer.resetHealthAndHunger(toKill);
            toKill.setAlive(false);
            List<ClockworkParticipant> awardedParticipants = survivingParticipants.stream()
                    .filter(p -> !p.getTeamId().equals(toKill.getTeamId()))
                    .toList();
            
            // messaging start
            Component claimedByTimeMessage = Component.empty()
                    .append(toKill.displayName())
                    .append(Component.text(" was claimed by time"));
            List<ClockworkParticipant> nonAwardedParticipants = context.getParticipants().values().stream()
                    .filter(p -> !awardedParticipants.contains(p))
                    .toList();
            Audience.audience(
                    Audience.audience(nonAwardedParticipants),
                    context.getAdminsAudience()
            ).sendMessage(claimedByTimeMessage);
            context.addPointsMessage(config.getPlayerEliminationScore(), Audience.audience(awardedParticipants), claimedByTimeMessage);
            // messaging end
            
            // award living participants start
            chain = chain.thenComposeAsync(
                    v -> context.awardParticipantPoints(awardedParticipants, config.getPlayerEliminationScore(), String.format("Participant \"%s\" was eliminated", toKill.getName())),
                    context.getGameManager().getMainThreadExecutor()
            );
            // award living participants end
        }
        context.getTabList().setParticipantGreys(participantsToKill, true);
        // who are now dead, which weren't at the start of this method
        List<ClockworkTeam> newlyKilledTeams = context.getTeams().values().stream()
                .filter(t -> !existingDeadTeams.contains(t))
                .filter(ClockworkTeam::isDead)
                .toList();
        if (newlyKilledTeams.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        List<ClockworkTeam> survivingTeams = context.getTeams().values().stream()
                .filter(ClockworkTeam::isAlive)
                .filter(t -> !newlyKilledTeams.contains(t))
                .toList();
        for (ClockworkTeam newlyKilledTeam : newlyKilledTeams) {
            Component teamEliminationMessage = Component.empty()
                    .append(newlyKilledTeam.getFormattedDisplayName())
                    .append(Component.text(" has been eliminated"));
            newlyKilledTeam.sendMessage(teamEliminationMessage
                    .color(NamedTextColor.DARK_RED));
            Component opponentEliminationMessage = teamEliminationMessage
                    .color(NamedTextColor.GREEN);
            
            List<ClockworkTeam> nonSurvivingTeams = context.getTeams().values().stream()
                    .filter(t -> !survivingTeams.contains(t) && !t.equals(newlyKilledTeam))
                    .toList();
            Audience.audience(
                    Audience.audience(nonSurvivingTeams),
                    context.getAdminsAudience()
            ).sendMessage(opponentEliminationMessage);
            context.addPointsMessage(config.getTeamEliminationScore(), Audience.audience(survivingTeams), opponentEliminationMessage);
            
            chain = chain.thenComposeAsync(
                    v -> context.awardTeamPoints(survivingTeams, config.getTeamEliminationScore(), String.format("team \"%s\" was eliminated", newlyKilledTeam.getTeamId())),
                    context.getGameManager().getMainThreadExecutor()
            );
        }
        return chain;
    }
    
    @Override
    public void onNewParticipantJoin(ClockworkParticipant participant, ClockworkTeam team) {
        super.onNewParticipantJoin(participant, team);
        participant.setAlive(false);
        context.getTabList().setParticipantGrey(participant, true);
        participant.setGameMode(GameMode.SPECTATOR);
        participant.teleport(context.getConfig().getStartingLocation());
    }
    
    @Override
    public void onParticipantRejoin(ClockworkParticipant participant, ClockworkTeam team) {
        super.onParticipantRejoin(participant, team);
        participant.setAlive(false);
        context.getTabList().setParticipantGrey(participant, true);
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
