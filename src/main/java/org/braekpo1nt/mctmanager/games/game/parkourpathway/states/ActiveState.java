package org.braekpo1nt.mctmanager.games.game.parkourpathway.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourParticipant;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourPathwayGame;
import org.braekpo1nt.mctmanager.ui.TimeStringUtils;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ActiveState extends ActiveStateBase {
    private final Timer mainTimer;
    /**
     * a countdown that restarts every time a player reaches a new checkpoint.
     * If players don't reach a new checkpoint by the time it runs out,
     * the game ends for good.
     * This has two assignments, one that doesn't show players a status and
     * a "last X seconds" type warning. The timer can be restarted
     * at any point until the "last X seconds" runs out.
     */
    private @Nullable Timer mercyRuleTimer;
    
    public ActiveState(@NotNull ParkourPathwayGame context) {
        super(context);
        restartMercyRuleCountdown();
        for (ParkourParticipant participant : context.getParticipants().values()) {
            context.giveSkipItem(participant, config.getNumOfSkips());
        }
        mainTimer = context.getTimerManager().start(Timer.builder()
                .duration(config.getTimeLimitDuration())
                .completionSeconds(config.getMercyRuleAlertDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .onCompletion(() -> {
                    if (mercyRuleTimer != null) {
                        mercyRuleTimer.cancel();
                    }
                    context.setState(new EndingState(context));
                })
                .build());
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        if (this.mercyRuleTimer != null) {
            this.mercyRuleTimer.cancel();
        }
    }
    
    private void restartMercyRuleCountdown() {
        if (this.mercyRuleTimer != null) {
            this.mercyRuleTimer.cancel();
        }
        context.getSidebar().updateLine("ending", Component.empty());
        context.getAdminSidebar().updateLine("ending", Component.empty());
        this.mercyRuleTimer = Timer.builder()
                .duration(config.getMercyRuleDuration())
                .completionSeconds(config.getMercyRuleAlertDuration())
                .withSidebar(context.getAdminSidebar(), "ending")
                .sidebarPrefix(Component.text("Mercy Rule: "))
                .onCompletion(() -> {
                    Component timeLeft = TimeStringUtils.getTimeComponent(config.getMercyRuleAlertDuration());
                    context.messageAllParticipants(Component.text("No one has reached a new checkpoint in the last ")
                            .append(TimeStringUtils.getTimeComponent(config.getMercyRuleDuration()))
                            .append(Component.text(". Ending in "))
                            .append(timeLeft)
                            .append(Component.text("."))
                            .color(NamedTextColor.RED));
                    context.titleAllParticipants(UIUtils.defaultTitle(
                            Component.empty(),
                            Component.empty()
                                    .append(timeLeft)
                                    .append(Component.text(" left"))
                                    .color(NamedTextColor.RED))
                    );
                    startMercyRuleFinalCountdown();
                })
                .build().start(context.getPlugin());
    }
    
    private void startMercyRuleFinalCountdown() {
        if (this.mercyRuleTimer != null) {
            this.mercyRuleTimer.cancel();
        }
        this.mercyRuleTimer = Timer.builder()
                .duration(config.getMercyRuleAlertDuration())
                .withSidebar(context.getSidebar(), "ending")
                .withSidebar(context.getAdminSidebar(), "ending")
                .sidebarPrefix(Component.text("Ending in: ")
                        .color(NamedTextColor.RED))
                .timerColor(NamedTextColor.RED)
                .onCompletion(() -> {
                    context.messageAllParticipants(Component.text("No one has reached a new checkpoint in the last ")
                            .append(TimeStringUtils.getTimeComponent(config.getMercyRuleDuration()))
                            .append(Component.text(". Stopping early")));
                    for (ParkourParticipant participant : context.getParticipants().values()) {
                        context.awardPointsForUnusedSkips(participant);
                        participant.setGameMode(GameMode.SPECTATOR);
                    }
                    mainTimer.cancel();
                    context.getSidebar().updateLine("timer", Component.empty());
                    context.getAdminSidebar().updateLine("timer", Component.empty());
                    context.setState(new GameOverState(context));
                })
                .build().start(context.getPlugin());
    }
}
