package org.braekpo1nt.mctmanager.games.game.farmrush.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.games.game.farmrush.Arena;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushTeam;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class DescriptionState implements FarmRushState {
    
    protected final @NotNull FarmRushGame context;
    
    public DescriptionState(@NotNull FarmRushGame context) {
        this.context = context;
        startTimer();
    }
    
    protected void startTimer() {
        context.messageAllParticipants(context.getConfig().getDescription());
        if (context.getConfig().shouldEnforceMaxScore()) {
            context.messageAllParticipants(Component.empty()
                    .append(Component.text("The first team to reach "))
                    .append(Component.text((int) (context.getConfig().getMaxScore() * context.getGameManager().matchProgressPointMultiplier()))
                            .color(NamedTextColor.GOLD)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" points wins!"))
            );
        }
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getDescriptionDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Starting soon: "))
                .onCompletion(() -> context.setState(new StartingState(context)))
                .build());
    }
    
    @Override
    public void onTeamJoin(Team team) {
        if (context.getTeams().containsKey(team.getTeamId())) {
            return;
        }
        Arena newArena;
        FarmRushTeam lastInLine = context.getLastTeamInLine();
        int newOrder;
        if (lastInLine == null) {
            newArena = context.getConfig().getFirstArena();
            newOrder = 0;
        } else {
            Vector offset = new Vector(context.getConfig().getFirstArena().getBounds().getWidthX() + 1, 0, 0);
            newArena = lastInLine.getArena().offset(offset);
            newOrder = lastInLine.getArenaOrder() + 1;
        }
        FarmRushTeam farmRushTeam = new FarmRushTeam(team, newArena, newOrder);
        context.getTeams().put(farmRushTeam.getTeamId(), farmRushTeam);
        context.placeArenas(Collections.singletonList(newArena));
    }
    
    @Override
    public void onParticipantJoin(Participant player) {
        context.initializeParticipant(player);
        context.getSidebar().updateLine(player.getUniqueId(), "title", context.getTitle());
        player.sendMessage(context.getConfig().getDescription());
    }
    
    @Override
    public void onParticipantQuit(Participant participant) {
        context.resetParticipant(participant);
        context.getParticipants().remove(participant.getUniqueId());
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event, Participant participant) {
        Arena arena = context.getTeams().get(participant.getTeamId()).getArena();
        if (!arena.getBarn().contains(event.getFrom().toVector())) {
            participant.teleport(arena.getSpawn());
            return;
        }
        if (!arena.getBarn().contains(event.getTo().toVector())) {
            event.setCancelled(true);
        }
    }
}
