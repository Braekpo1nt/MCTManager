package org.braekpo1nt.mctmanager.games.game.example.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.example.ExampleGame;
import org.braekpo1nt.mctmanager.ui.timer.Timer;

public class GameOverState extends ExampleStateBase {
    public GameOverState(ExampleGame context) {
        super(context);
        Audience.audience(
                Audience.audience(context.getParticipants().values()),
                Audience.audience(context.getAdmins())
        ).sendMessage(Component.empty()
                .append(Component.text("The GameOverState has begin")));
        context.getTimerManager().start(Timer.builder()
                .duration(10)
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Game Over: "))
                .onCompletion(context::stop)
                .build());
    }
    
}
