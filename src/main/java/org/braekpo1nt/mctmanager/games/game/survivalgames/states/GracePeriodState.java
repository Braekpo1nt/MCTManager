package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesGame;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesParticipant;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GracePeriodState extends RoundActiveState {
    
    private @Nullable Timer gracePeriodTimer;
    
    public GracePeriodState(@NotNull SurvivalGamesGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        super.enter();
        context.removePlatforms();
        Component gracePeriodDuration = TimeStringUtils.getTimeComponent(config.getGracePeriodDuration());
        Component gracePeriodStarted = Component.empty()
                .append(gracePeriodDuration)
                .append(Component.text(" grace period"))
                .color(NamedTextColor.GREEN);
        context.messageAllParticipants(gracePeriodStarted);
        Audience.audience(context.getParticipants().values()).showTitle(UIUtils.defaultTitle(
                Component.empty(),
                gracePeriodStarted
        ));
        gracePeriodTimer = timerManager.start(Timer.builder()
                .duration(config.getGracePeriodDuration())
                .withTopbar(topbar)
                .topbarPrefix(Component.text("Grace Period: "))
                .onCompletion(() -> {
                    Component gracePeriodEnded = Component.empty()
                            .append(Component.text("Grace period ended"))
                            .color(NamedTextColor.RED);
                    context.messageAllParticipants(gracePeriodEnded);
                    context.titleAllParticipants(UIUtils.defaultTitle(
                            Component.empty(),
                            gracePeriodEnded
                    ));
                    context.setState(new BorderDelayState(context));
                })
                .build());
    }
    
    @Override
    protected boolean allowRespawn() {
        return true;
    }
    
    @Override
    public void exit() {
        Timer.cancel(gracePeriodTimer);
        super.exit();
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull SurvivalGamesParticipant participant) {
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "SurvivalGames.ActiveState.onPlayerDamage()->invulnerable cancelled");
        event.setCancelled(true);
    }
}
