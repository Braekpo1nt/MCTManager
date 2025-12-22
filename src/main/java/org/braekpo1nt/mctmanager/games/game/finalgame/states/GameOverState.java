package org.braekpo1nt.mctmanager.games.game.finalgame.states;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.base.Affiliation;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalGame;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalParticipant;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.GameMode;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GameOverState extends FinalStateBase {
    
    private @Nullable Timer timer;
    
    public GameOverState(@NotNull FinalGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        context.getAdminSidebar().addLine("over", "");
        timer = context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getGameOverDuration())
                .withSidebar(context.getAdminSidebar(), "over")
                .sidebarPrefix(Component.text("Game Over: "))
                .withTopbar(context.getTopbar())
                .onCompletion(() -> {
                    context.getAdminSidebar().deleteLine("over");
                    context.stop();
                })
                .build());
    }
    
    @Override
    public void exit() {
        Timer.cancel(timer);
    }
    
    @Override
    public void onParticipantRespawn(@NotNull PlayerRespawnEvent event, @NotNull FinalParticipant participant) {
        super.onParticipantRespawn(event, participant);
        if (participant.getAffiliation() == Affiliation.SPECTATOR) {
            return;
        }
        event.setRespawnLocation(participant.getLocation());
    }
    
    @Override
    public void onParticipantPostRespawn(@Nullable PlayerPostRespawnEvent event, @NotNull FinalParticipant participant) {
        if (participant.getAffiliation() == Affiliation.SPECTATOR) {
            return;
        }
        participant.setGameMode(GameMode.SPECTATOR);
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.clearInventory(participant);
    }
}
