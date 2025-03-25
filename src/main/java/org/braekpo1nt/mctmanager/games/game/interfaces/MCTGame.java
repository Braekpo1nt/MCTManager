package org.braekpo1nt.mctmanager.games.game.interfaces;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * An MCT game. 
 */
public interface MCTGame {
    GameType getType();
    void stop();
    
    void onParticipantJoin(Participant participant, Team team);
    void onParticipantQuit(UUID participantUUID, String teamId);
    
    void onAdminJoin(Player admin);
    void onAdminQuit(Player admin);
    void setTitle(@NotNull Component title);
}
