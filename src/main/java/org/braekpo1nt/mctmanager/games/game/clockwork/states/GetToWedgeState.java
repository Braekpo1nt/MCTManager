package org.braekpo1nt.mctmanager.games.game.clockwork.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkGame;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkTeam;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

public class GetToWedgeState extends ClockworkStateBase {
    public GetToWedgeState(@NotNull ClockworkGame context) {
        super(context);
        turnOnCollisions();
        if (context.getConfig().getGetToWedgeMessage() != null) {
            context.titleAllParticipants(UIUtils.defaultTitle(
                    Component.empty(),
                    context.getConfig().getGetToWedgeMessage()
            ));
        }
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getGetToWedgeDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Get to wedge! "))
                .onCompletion(() -> context.setState(new StayOnWedgeState(context)))
                .name("startGetToWedgeDelay")
                .build());
    }
    
    private void turnOnCollisions() {
        for (ClockworkTeam team : context.getTeams().values()) {
            context.setTeamOption(team, Team.Option.COLLISION_RULE, context.getConfig().getCollisionRule());
        }
    }
    
    @Override
    public void onTeamRejoin(ClockworkTeam team) {
        super.onNewTeamJoin(team);
        context.setTeamOption(team, Team.Option.COLLISION_RULE, context.getConfig().getCollisionRule());
    }
    
    @Override
    public void onNewTeamJoin(ClockworkTeam team) {
        super.onNewTeamJoin(team);
        context.setTeamOption(team, Team.Option.COLLISION_RULE, context.getConfig().getCollisionRule());
    }
}
