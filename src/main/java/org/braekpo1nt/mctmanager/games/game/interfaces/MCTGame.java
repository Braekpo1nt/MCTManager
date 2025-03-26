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
    
    /**
     * @deprecated use {@link #onParticipantJoin(Participant)}
     */
    @Deprecated
    void onParticipantJoin(Participant participant, Team team);
    /**
     * @deprecated use {@link #onParticipantQuit(UUID)}
     */
    @Deprecated
    void onParticipantQuit(UUID participantUUID, String teamId);
    // TODO: change GameManager to call onTeamJoin before onParticipantJoin
    default void onTeamJoin(Team team) {
        // TODO: require implementation
    }
    default void onParticipantJoin(Participant participant) {
        // TODO: require implementation
    }
    default void onParticipantQuit(UUID participantUUID) {
        // TODO: require implementation
    }
    default void onTeamQuit(String teamId) {
        // TODO: require implementation
    }
    
    void onAdminJoin(Player admin);
    void onAdminQuit(Player admin);
    void setTitle(@NotNull Component title);
}
