package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import org.braekpo1nt.mctmanager.participant.Participant;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public interface SurvivalGamesState {
    void onParticipantJoin(Participant participant);
    void onParticipantQuit(Participant participant);
    void initializeParticipant(Participant participant);
    void resetParticipant(Participant participant);
    
    void onPlayerDamage(EntityDamageEvent event);
    void onParticipantDeath(PlayerDeathEvent event);
}
