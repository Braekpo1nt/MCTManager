package org.braekpo1nt.mctmanager.games.game.mecha.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.mecha.MechaGame;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

public class GameOverState implements MechaState {
    private final @NotNull MechaGame context;
    
    public GameOverState(@NotNull MechaGame context) {
        this.context = context;
        context.getSidebar().addLine("ending", "");
        context.getAdminSidebar().addLine("ending", "");
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getEndDuration())
                .withSidebar(context.getAdminSidebar(), "ending")
                .sidebarPrefix(Component.text("Game ending: "))
                .onCompletion(() -> {
                    context.getSidebar().deleteLine("ending");
                    context.getAdminSidebar().deleteLine("ending");
                    context.stop();
                })
                .build());
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        // do nothing
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        // do nothing
    }
    
    @Override
    public void initializeParticipant(Player participant) {
        // not used
    }
    
    @Override
    public void resetParticipant(Player participant) {
        context.resetParticipant(participant);
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            return;
        }
        event.setCancelled(true);
    }
    
    @Override
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setCancelled(true);
    }
}
