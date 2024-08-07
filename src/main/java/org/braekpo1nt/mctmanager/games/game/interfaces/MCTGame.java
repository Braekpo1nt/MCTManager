package org.braekpo1nt.mctmanager.games.game.interfaces;

import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An MCT game. 
 */
public interface MCTGame {
    GameType getType();
    void start(List<Player> newParticipants, List<Player> newAdmins);
    void stop();
    void onParticipantJoin(Player participant);
    void onParticipantQuit(Player participant);
    
    void onAdminJoin(Player admin);
    void onAdminQuit(Player admin);
    @NotNull String getBaseTitle();
    void setTitle(@NotNull String title);
}
