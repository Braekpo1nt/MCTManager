package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public interface SurvivalGamesState {
    void onParticipantJoin(Player participant);
    void onParticipantQuit(Player participant);
    void initializeParticipant(Participant participant);
    void resetParticipant(Player participant);
    
    void onPlayerDamage(EntityDamageEvent event);
    void onPlayerDeath(PlayerDeathEvent event);
}
