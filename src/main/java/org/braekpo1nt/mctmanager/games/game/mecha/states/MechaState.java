package org.braekpo1nt.mctmanager.games.game.mecha.states;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public interface MechaState {
    void onParticipantJoin(Player participant);
    void onParticipantQuit(Player participant);
    void initializeParticipant(Player participant);
    void resetParticipant(Player participant);
    
    void onPlayerDamage(EntityDamageEvent event);
    void onPlayerDeath(PlayerDeathEvent event);
}
