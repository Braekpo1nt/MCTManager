package org.braekpo1nt.mctmanager.games.game.example.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.example.ExampleGame;
import org.braekpo1nt.mctmanager.games.game.example.ExampleParticipant;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class ActiveState extends ExampleState {
    public ActiveState(ExampleGame context) {
        super(context);
        Audience.audience(
                Audience.audience(context.getParticipants().values()),
                Audience.audience(context.getAdmins())
        ).sendMessage(Component.empty()
                .append(Component.text("The ActiveState has begun")));
        context.getTimerManager().start(Timer.builder()
                .duration(60)
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Playing: "))
                .onCompletion(() -> {
                    context.setState(new GameOverState(context));
                })
                .build());
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event, ExampleParticipant participant) {
        int fromY = event.getFrom().getBlockY();
        int toY = event.getTo().getBlockY();
        if (toY - fromY < 1) {
            return;
        }
        Main.logf("%s jumped one block", participant.getName());
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event, ExampleParticipant participant) {
        // do nothing
        Main.logf("%s was damaged %s", participant.getName(), event.getDamage());
    }
}
