package org.braekpo1nt.mctmanager.games.game.footrace.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceGame;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceParticipant;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceTeam;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public abstract class FootRaceStateBase implements FootRaceState {
    
    protected final @NotNull FootRaceGame context;
    
    protected FootRaceStateBase(@NotNull FootRaceGame context) {
        this.context = context;
    }
    
    @Override
    public void cleanup() {
        
    }
    
    @Override
    public void onTeamRejoin(FootRaceTeam team) {
        
    }
    
    @Override
    public void onNewTeamJoin(FootRaceTeam team) {
        
    }
    
    @Override
    public void onParticipantRejoin(FootRaceParticipant participant, FootRaceTeam team) {
        context.getStandings().add(participant);
        context.giveBoots(participant);
        participant.setRespawnLocation(context.getConfig().getStartingLocation(), true);
        context.updateStandings();
        context.displayStandings();
        context.getSidebar().updateLine(participant.getUniqueId(), "lap",
                Component.empty()
                        .append(Component.text("Lap: "))
                        .append(Component.text(participant.getLap()))
                        .append(Component.text("/"))
                        .append(Component.text(context.getConfig().getLaps())));
    }
    
    @Override
    public void onNewParticipantJoin(FootRaceParticipant participant, FootRaceTeam team) {
        context.getStandings().add(participant);
        participant.teleport(context.getConfig().getStartingLocation());
        participant.setRespawnLocation(context.getConfig().getStartingLocation(), true);
        context.giveBoots(participant);
        context.updateStandings();
        context.displayStandings();
        context.getSidebar().updateLine(participant.getUniqueId(), "lap",
                Component.empty()
                        .append(Component.text("Lap: "))
                        .append(Component.text(participant.getLap()))
                        .append(Component.text("/"))
                        .append(Component.text(context.getConfig().getLaps())));
    }
    
    @Override
    public void onParticipantQuit(FootRaceParticipant participant, FootRaceTeam team) {
        context.getStandings().remove(participant);
    }
    
    @Override
    public void onTeamQuit(FootRaceTeam team) {
        
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull FootRaceParticipant participant) {
        // do nothing
    }
    
    @Override
    public void onParticipantTeleport(@NotNull PlayerTeleportEvent event, @NotNull FootRaceParticipant participant) {
        
    }
    
    @Override
    public void onParticipantInteract(@NotNull PlayerInteractEvent event, @NotNull FootRaceParticipant participant) {
        
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull FootRaceParticipant participant) {
        event.setCancelled(true);
    }
}
