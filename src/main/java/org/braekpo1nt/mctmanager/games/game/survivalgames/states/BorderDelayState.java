package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.game.survivalgames.BorderStage;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesGame;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BorderDelayState extends RoundActiveState {
    
    protected @Nullable Timer borderDelay;
    
    public BorderDelayState(@NotNull SurvivalGamesGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        super.enter();
        BorderStage borderStage = context.getCurrentBorderStage();
        updateRespawnLine();
        announceCurrentRespawnState();
        borderDelay = timerManager.start(Timer.builder()
                .duration(borderStage.getDelay())
                .withSidebar(adminSidebar, "timer")
                .withTopbar(topbar)
                .sidebarPrefix(Component.text("Border: ")
                        .color(NamedTextColor.LIGHT_PURPLE))
                .topbarPrefix(Component.text("Border: ")
                        .color(NamedTextColor.LIGHT_PURPLE))
                .timerColor(NamedTextColor.LIGHT_PURPLE)
                .onCompletion(() -> {
                    int size = borderStage.getSize();
                    int duration = borderStage.getDuration();
                    worldBorder.setSize(size, duration);
                    sendBorderShrinkAnnouncement(duration, size);
                    context.setState(new BorderShrinkingState(context));
                })
                .build());
    }
    
    /**
     * Sends a chat message to all participants saying the border is shrinking
     * @param duration The duration of the shrink in seconds
     * @param size The size of the border in blocks
     */
    private void sendBorderShrinkAnnouncement(int duration, int size) {
        String timeString = TimeStringUtils.getTimeString(duration);
        context.messageAllParticipants(Component.empty()
                .append(Component.text("Border shrinking to "))
                .append(Component.text(size))
                .append(Component.text(" for "))
                .append(Component.text(timeString))
                .color(NamedTextColor.RED)
        );
        Audience.audience(
                Audience.audience(context.getAdmins()),
                Audience.audience(context.getParticipants().values())
        ).showTitle(UIUtils.defaultTitle(
                Component.empty(),
                Component.text("Border shrinking")
                        .color(NamedTextColor.RED)
        ));
    }
    
    /**
     * Performs appropriate announcements at appropriate times to let players know
     * what to expect when it comes to respawning. 
     * Takes into account the current border stage and the next stage when it changes (if at all)
     */
    private void announceCurrentRespawnState() {
        if (config.getBorder().neverRespawn()) {
            return;
        }
        int stageIndex = context.getBorderStageIndex();
        if (!config.getBorder().didRespawnStateChange(stageIndex)) {
            return;
        }
        if (config.getBorder().allowRespawn(stageIndex)) {
            context.messageAllParticipants(Component.text("Respawning is enabled")
                    .color(NamedTextColor.GREEN));
        } else {
            if (stageIndex != 0) {
                // don't overwrite the grace period over warning
                context.titleAllParticipants(UIUtils.defaultTitle(
                        Component.empty(),
                        Component.empty()
                                .append(Component.text("Respawning Disabled")
                                        .color(NamedTextColor.RED))
                ));
            }
            context.messageAllParticipants(Component.text("Respawning is disabled")
                    .color(NamedTextColor.RED));
        }
    }
    
    @Override
    public void exit() {
        if (borderDelay != null) {
            borderDelay.cancel();
        }
        super.exit();
    }
}
