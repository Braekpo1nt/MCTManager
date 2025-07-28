package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesGame;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesParticipant;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesTeam;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.bukkit.GameMode;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Common functionality for when players should be on their platforms,
 * new teams should get a new platform, and no damage should be taken
 */
public abstract class OnPlatformsState extends SurvivalGamesStateBase {
    
    public OnPlatformsState(@NotNull SurvivalGamesGame context) {
        super(context);
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
