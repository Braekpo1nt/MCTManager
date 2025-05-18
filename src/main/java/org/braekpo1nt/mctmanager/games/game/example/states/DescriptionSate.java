package org.braekpo1nt.mctmanager.games.game.example.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.example.ExampleGame;
import org.braekpo1nt.mctmanager.ui.timer.Timer;

public class DescriptionSate extends ExampleStateBase {
    public DescriptionSate(ExampleGame context) {
        super(context);
        Audience audience = Audience.audience(
                Audience.audience(context.getParticipants().values()),
                Audience.audience(context.getAdmins())
        );
        audience.sendMessage(Component.empty()
                .append(Component.text("The DescriptionState has begin")));
        audience.sendMessage(context.getConfig().getDescription());
        context.getTimerManager().start(Timer.builder()
                .duration(10)
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .withTopbar(context.getTopbar())
                .topbarPrefix(Component.text("Starting in: "))
                .sidebarPrefix(Component.text("Starting in: "))
                .onCompletion(() -> {
                    context.setState(new ActiveState(context));
                })
                .build());
    }
    
}
