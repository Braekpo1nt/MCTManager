package org.braekpo1nt.mctmanager.games.game.clockwork.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkGame;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkParticipant;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkTeam;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BreatherState extends RoundActiveState {
    
    private @Nullable Timer timer;
    
    public BreatherState(@NotNull ClockworkGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        context.getChaosManager().pause();
        turnOffCollisions();
        for (ClockworkParticipant participant : context.getParticipants().values()) {
            if (participant.isAlive()) {
                participant.teleport(config.getStartingLocation());
                participant.setArrowsInBody(0);
            }
        }
        timer = context.getTimerManager().start(Timer.builder()
                .duration(config.getBreatherDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Clock chimes in: "))
                .onCompletion(() -> {
                    context.setState(new ClockChimeState(context));
                })
                .name("startBreatherDelay")
                .build());
    }
    
    @Override
    public void exit() {
        if (timer != null) {
            timer.cancel();
        }
    }
    
    private void turnOffCollisions() {
        for (ClockworkTeam team : context.getTeams().values()) {
            context.setTeamOption(team, Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull ClockworkParticipant participant) {
        if (!participant.isAlive()) {
            return;
        }
        Location stayLoc = event.getTo();
        Vector position = config.getStartingLocation().toVector();
        if (!stayLoc.toVector().equals(position)) {
            participant.teleport(position.toLocation(stayLoc.getWorld(), stayLoc.getYaw(), stayLoc.getPitch()));
        }
    }
    
    @Override
    public void onTeamRejoin(ClockworkTeam team) {
        super.onNewTeamJoin(team);
        context.setTeamOption(team, Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
    }
    
    @Override
    public void onNewTeamJoin(ClockworkTeam team) {
        super.onNewTeamJoin(team);
        context.setTeamOption(team, Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
    }
}
