package org.braekpo1nt.mctmanager.games.game.farmrush.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.farmrush.Arena;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class DescriptionState implements FarmRushState {
    
    protected final @NotNull FarmRushGame context;
    
    public DescriptionState(@NotNull FarmRushGame context) {
        this.context = context;
        startTimer();
    }
    
    protected void startTimer() {
        context.messageAllParticipants(context.getConfig().getDescription());
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getDescriptionDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Starting soon: "))
                .onCompletion(() -> context.setState(new StartingState(context)))
                .build());
    }
    
    @Override
    public void onParticipantJoin(Player player) {
        String teamId = context.getGameManager().getTeamId(player.getUniqueId());
        boolean brandNewTeam = !context.getTeams().containsKey(teamId);
        if (brandNewTeam) {
            context.onNewTeamJoin(teamId);
        }
        context.initializeParticipant(player);
        context.getSidebar().updateLine(player.getUniqueId(), "title", context.getTitle());
        player.sendMessage(context.getConfig().getDescription());
    }
    
    @Override
    public void onParticipantQuit(FarmRushGame.Participant participant) {
        context.resetParticipant(participant);
        context.getParticipants().remove(participant.getUniqueId());
        context.getTeams().get(participant.getTeamId()).getMembers().remove(participant.getUniqueId());
    }
}
