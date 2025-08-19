package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.game.survivalgames.BorderStage;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesGame;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BorderShrinkingState extends RoundActiveState {
    
    protected @Nullable Timer borderShrinking;
    
    public BorderShrinkingState(@NotNull SurvivalGamesGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        super.enter();
        BorderStage borderStage = context.getCurrentBorderStage();
        updateRespawnLine();
        warnAboutRespawnChange();
        borderShrinking = timerManager.start(Timer.builder()
                .duration(borderStage.getDuration())
                .withSidebar(adminSidebar, "timer")
                .withTopbar(topbar)
                .sidebarPrefix(Component.text("Border shrinking: ")
                        .color(NamedTextColor.RED))
                .topbarPrefix(Component.text("Border shrinking: ")
                        .color(NamedTextColor.RED))
                .timerColor(NamedTextColor.RED)
                .onCompletion(() -> {
                    context.setBorderStageIndex(context.getBorderStageIndex() + 1);
                    if (context.getBorderStageIndex() >= config.getBorderStages().size()) {
                        // continue to reference the final one
                        context.setBorderStageIndex(context.getBorderStageIndex() - 1);
                        context.setState(new SuddenDeathState(context));
                        return;
                    }
                    sendBorderDelayAnnouncement(borderStage.getDelay());
                    context.setState(new BorderDelayState(context));
                })
                .build());
    }
    
    /**
     * If the current stage allows respawning, but the next does not,
     * sends a warning to the participants
     */
    private void warnAboutRespawnChange() {
        if (config.getBorder().neverRespawn()) {
            return;
        }
        int stageIndex = context.getBorderStageIndex();
        if (!config.getBorder().willRespawnStateChange(stageIndex)) {
            return;
        }
        if (!config.getBorder().allowRespawn(stageIndex + 1)) {
            BorderStage currentBorderStage = context.getCurrentBorderStage();
            context.messageAllParticipants(Component.empty()
                            .append(Component.text("Respawning will be disabled in "))
                            .append(TimeStringUtils.getTimeComponent(currentBorderStage.getDuration()))
                            .color(NamedTextColor.RED));
        }
    }
    
    /**
     * Sends a chat message to all participants saying the border is delaying
     * @param delay The delay in seconds
     */
    private void sendBorderDelayAnnouncement(int delay) {
        String timeString = TimeStringUtils.getTimeString(delay);
        context.messageAllParticipants(Component.text("Border will not shrink for "+timeString));
    }
    
    @Override
    public void exit() {
        Timer.cancel(borderShrinking);
        super.exit();
    }
}
