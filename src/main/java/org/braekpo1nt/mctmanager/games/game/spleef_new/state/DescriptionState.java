package org.braekpo1nt.mctmanager.games.game.spleef_new.state;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.spleef_new.SpleefGame;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;

public class DescriptionState extends SpleefStateBase {
    
    public DescriptionState(@NotNull SpleefGame context) {
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
                .sidebarPrefix(Component.text("Starting in: "))
                .onCompletion(() -> {
                    context.setState(new PreRoundState(context));
                })
                .build());
    }
}
