package org.braekpo1nt.mctmanager.games.game.colossalcombat.states;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.base.Affiliation;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalCombatGame;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalParticipant;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.GameMode;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GameOverState extends ColossalCombatStateBase {
    
    private @Nullable Timer timer;
    
    public GameOverState(@NotNull ColossalCombatGame context) {
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
    public void onParticipantRespawn(@NotNull PlayerRespawnEvent event, @NotNull ColossalParticipant participant) {
        super.onParticipantRespawn(event, participant);
        if (participant.getAffiliation() == Affiliation.SPECTATOR) {
            return;
        }
        event.setRespawnLocation(participant.getLocation());
    }
    
    @Override
    public void onParticipantPostRespawn(PlayerPostRespawnEvent event, @NotNull ColossalParticipant participant) {
        if (participant.getAffiliation() == Affiliation.SPECTATOR) {
            return;
        }
        participant.setGameMode(GameMode.SPECTATOR);
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.clearInventory(participant);
    }
}
