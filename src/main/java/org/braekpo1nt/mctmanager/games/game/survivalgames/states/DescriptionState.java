package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesGame;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesTeam;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DescriptionState extends OnPlatformsState {
    
    private @Nullable Timer timer;
    
    public DescriptionState(@NotNull SurvivalGamesGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        for (SurvivalGamesTeam team : context.getTeams().values()) {
            context.updateAliveCount(team);
        }
        context.updateRoundLine();
        context.createPlatformsAndTeleportTeams();
        context.messageAllParticipants(context.getConfig().getDescription());
        timer = context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getDescriptionDuration())
                .withSidebar(context.getAdminSidebar(), "timer")
                .withTopbar(context.getTopbar())
                .sidebarPrefix(Component.text("Starting soon: "))
                .onCompletion(() -> {
                    if (context.getConfig().getRounds() <= 1) {
                        context.setState(new SinglePreRoundState(context));
                    } else {
                        context.setState(new MultiPreRoundState(context));
                    }
                })
                .build());
    }
    
    @Override
    public void exit() {
        Timer.cancel(timer);
    }
}
