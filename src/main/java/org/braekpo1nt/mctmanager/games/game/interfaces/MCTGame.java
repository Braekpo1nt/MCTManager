package org.braekpo1nt.mctmanager.games.game.interfaces;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * An MCT game.
 */
public interface MCTGame {
    GameType getType();
    
    void stop();
    
    void onTeamJoin(Team team);
    
    void onParticipantJoin(Participant participant);
    
    void onParticipantQuit(UUID participantUUID);
    
    void onTeamQuit(@NotNull String teamId);
    
    void onAdminJoin(Player admin);
    
    void onAdminQuit(Player admin);
    
    void setTitle(@NotNull Component title);
    
    boolean containsTeam(@NotNull String teamId);
    
    /**
     * @param uuid the UUID of the participant to run {@code /top}
     * @return the result of running the {@code /top} command
     */
    @NotNull CommandResult top(@NotNull UUID uuid);
}
