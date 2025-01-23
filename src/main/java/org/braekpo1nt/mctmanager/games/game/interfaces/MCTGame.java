package org.braekpo1nt.mctmanager.games.game.interfaces;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * An MCT game. 
 */
public interface MCTGame {
    GameType getType();
    void start(Collection<Team> newTeams, Collection<Participant> newParticipants, List<Player> newAdmins);
    void stop();
    
    /**
     * Called when a team gets its first online member after having no online members.
     * Called before {@link #onParticipantJoin(Participant)}
     * @param team the team who just joined (this is a copy of the GameManager's team instance)
     */
    default void onTeamJoin(Team team) {
        // do nothing
    }
    void onParticipantJoin(Participant participant);
    void onParticipantQuit(Participant participant);
    /**
     * Called when a team's last online member quits.
     * Called after {@link #onParticipantQuit(Participant)}
     * @param team the team who just quit (this is a copy of the GameManager's team instance)
     */
    default void onTeamQuit(Team team) {
        // do nothing
    }
    
    void onAdminJoin(Player admin);
    void onAdminQuit(Player admin);
    @NotNull Component getBaseTitle();
    void setTitle(@NotNull Component title);
}
