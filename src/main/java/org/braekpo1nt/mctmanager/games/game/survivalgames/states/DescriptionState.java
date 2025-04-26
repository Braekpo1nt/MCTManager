package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesGame;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesParticipant;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesTeam;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.GameMode;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

public class DescriptionState extends SurvivalGamesStateBase {
    
    public DescriptionState(@NotNull SurvivalGamesGame context) {
        super(context);
        startTimer();
    }
    
    protected void startTimer() {
        context.messageAllParticipants(context.getConfig().getDescription());
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getDescriptionDuration())
                .withSidebar(context.getAdminSidebar(), "timer")
                .withTopbar(context.getTopbar())
                .sidebarPrefix(Component.text("Starting soon: "))
                .onCompletion(() -> context.setState(new StartingState(context)))
                .build());
    }
    
    @Override
    public void onNewParticipantJoin(SurvivalGamesParticipant participant, SurvivalGamesTeam team) {
        super.onNewParticipantJoin(participant, team);
        context.createPlatformsAndTeleportTeams();
    }
    
    @Override
    public void onParticipantRejoin(SurvivalGamesParticipant participant, SurvivalGamesTeam team) {
        super.onParticipantRejoin(participant, team);
        context.createPlatformsAndTeleportTeams();
    }
    
    @Override
    public void onTeamQuit(SurvivalGamesTeam team) {
        context.createPlatformsAndTeleportTeams();
        super.onTeamQuit(team);
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull SurvivalGamesParticipant participant) {
        event.setCancelled(true);
    }
    
    @Override
    public void onParticipantPostRespawn(PlayerPostRespawnEvent event, SurvivalGamesParticipant participant) {
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.clearInventory(participant);
    }
}
