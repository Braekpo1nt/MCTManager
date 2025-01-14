package org.braekpo1nt.mctmanager.games.game.interfaces;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * An MCT game. 
 */
public interface MCTGame {
    GameType getType();
    void start(Collection<Participant> newParticipants, List<Player> newAdmins);
    void stop();
    void onParticipantJoin(Participant participant);
    void onParticipantQuit(Participant participant);
    
    void onAdminJoin(Player admin);
    void onAdminQuit(Player admin);
    @NotNull Component getBaseTitle();
    void setTitle(@NotNull Component title);
}
