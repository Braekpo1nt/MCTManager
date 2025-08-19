package org.braekpo1nt.mctmanager.games.game.farmrush.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushParticipant;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * After the timer has reached the grace period limit, or at least one
 * team has reached the score limit
 * This is different from {@link ActiveState} because it's not checking for
 * a team winning, and has a red countdown timer.
 */
public class GracePeriodState extends GameplayState {
    
    private @Nullable Timer timer;
    
    public GracePeriodState(@NotNull FarmRushGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        timer = context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getGracePeriodDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Ending: ")
                        .color(NamedTextColor.RED))
                .timerColor(NamedTextColor.RED)
                .onCompletion(() -> context.setState(new GameOverState(context)))
                .build());
    }
    
    @Override
    public void exit() {
        Timer.cancel(timer);
    }
    
    @Override
    public void onParticipantCloseInventory(InventoryCloseEvent event, FarmRushParticipant participant) {
        sellItemsOnCloseInventory(event, participant);
    }
}
