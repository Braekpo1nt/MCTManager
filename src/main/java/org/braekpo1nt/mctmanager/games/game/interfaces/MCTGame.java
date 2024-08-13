package org.braekpo1nt.mctmanager.games.game.interfaces;

import net.kyori.adventure.text.Component;
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
    @NotNull Component getBaseTitle();
    void setTitle(@NotNull Component title);
}
