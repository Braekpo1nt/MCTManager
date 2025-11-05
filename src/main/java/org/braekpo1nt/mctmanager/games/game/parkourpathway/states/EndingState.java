package org.braekpo1nt.mctmanager.games.game.parkourpathway.states;

import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourParticipant;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourPathwayGame;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayConfig;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EndingState extends GamePlayState {
    
    private @Nullable Timer timer;
    
    public EndingState(@NotNull ParkourPathwayGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        ParkourPathwayConfig config = context.getConfig();
        timer = context.getTimerManager().start(Timer.builder()
                .duration(config.getMercyRuleAlertDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .timerColor(NamedTextColor.RED)
                .onCompletion(() -> {
                    for (ParkourParticipant participant : context.getParticipants().values()) {
                        context.awardPointsForUnusedSkips(participant);
                        participant.setGameMode(GameMode.SPECTATOR);
                    }
                    context.getPlugin().getServer().getScheduler().cancelTask(skipCooldownTaskId);
                    context.setState(new GameOverState(context));
                })
                .build());
    }
    
    @Override
    public void exit() {
        context.getPlugin().getServer().getScheduler().cancelTask(skipCooldownTaskId);
        Timer.cancel(timer);
    }
    
    @Override
    protected void restartMercyRuleCountdown() {
        // do nothing
    }
    
    @Override
    protected void stop() {
        context.getPlugin().getServer().getScheduler().cancelTask(skipCooldownTaskId);
        context.setState(new GameOverState(context));
    }
}
