package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import org.braekpo1nt.mctmanager.games.experimental.GameStateBase;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesParticipant;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesTeam;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public interface SurvivalGamesState extends GameStateBase<SurvivalGamesParticipant, SurvivalGamesTeam> {
    void onParticipantDeath(PlayerDeathEvent event);
}
