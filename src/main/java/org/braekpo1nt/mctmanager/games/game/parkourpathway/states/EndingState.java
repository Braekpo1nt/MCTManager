package org.braekpo1nt.mctmanager.games.game.parkourpathway.states;

import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourParticipant;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourPathwayGame;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayConfig;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;

public class EndingState extends GamePlayState {
    
    public EndingState(@NotNull ParkourPathwayGame context) {
        super(context);
        ParkourPathwayConfig config = context.getConfig();
        context.getTimerManager().start(Timer.builder()
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
    protected void restartMercyRuleCountdown() {
        // do nothing
    }
    
    @Override
    protected void stop() {
        context.getPlugin().getServer().getScheduler().cancelTask(skipCooldownTaskId);
        context.setState(new GameOverState(context));
    }
}
