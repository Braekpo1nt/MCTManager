package org.braekpo1nt.mctmanager.games.game.example.states;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.experimental.GameStateBase;
import org.braekpo1nt.mctmanager.games.game.example.ExampleGame;
import org.braekpo1nt.mctmanager.games.game.example.ExampleParticipant;
import org.braekpo1nt.mctmanager.games.game.example.ExampleTeam;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public abstract class ExampleState implements GameStateBase<ExampleParticipant, ExampleTeam> {
    
    protected final ExampleGame context;
    
    public ExampleState(ExampleGame context) {
        this.context = context;
    }
    
    @Override
    public void cleanup() {
        // custom stop code
        Main.logger().info("Cleaning up the current state");
    }
    
    @Override
    public void onTeamRejoin(ExampleTeam team) {
        // custom team rejoin code
        Main.logf("First team member of re-joining team %s joined", team.getTeamId());
    }
    
    @Override
    public void onNewTeamJoin(ExampleTeam team) {
        // custom team join code
        Main.logf("First team member of new team %s joined", team.getTeamId());
    }
    
    @Override
    public void onParticipantRejoin(ExampleParticipant participant, ExampleTeam team) {
        // custom participant rejoin code
        Main.logf("%s re-joined the game, after quitting", participant.getName());
    }
    
    @Override
    public void onNewParticipantJoin(ExampleParticipant participant, ExampleTeam team) {
        // custom participant join code
        Main.logf("%s joined the game for the first time", participant.getName());
        participant.teleport(context.getConfig().getStartingLocation());
    }
    
    @Override
    public void onParticipantQuit(ExampleParticipant participant, ExampleTeam team) {
        // custom participant quit code
        Main.logf("%s quit the game", participant.getName());
    }
    
    @Override
    public void onTeamQuit(ExampleTeam team) {
        // custom team quit code
        Main.logf("Last team member of %s quit", team.getTeamId());
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull ExampleParticipant participant) {
        // do nothing
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull ExampleParticipant participant) {
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "ExampleState.onPlayerDamage()");
        event.setCancelled(true);
    }
    
    @Override
    public void onParticipantInteract(@NotNull PlayerInteractEvent event, @NotNull ExampleParticipant participant) {
        // do nothing
    }
    
    @Override
    public void onParticipantTeleport(@NotNull PlayerTeleportEvent event, @NotNull ExampleParticipant participant) {
        // do nothing
    }
}
