package org.braekpo1nt.mctmanager.ui.tablist;

import net.kyori.adventure.text.format.TextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.participant.Team;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class EmptyTabList extends TabList {
    public EmptyTabList(@NotNull Main plugin) {
        super(plugin);
    }
    
    
    @Override
    public void addTeam(@NotNull String teamId, @NotNull String displayName, @NotNull TextColor color) {
    }
    
    @Override
    public void removeTeam(@NotNull String teamId) {
    }
    
    /**
     * Set the score of the given team
     * @param teamId the teamId to update the score of. Must be a valid teamId in this TabList.
     * @param score the score to update to.
     */
    @Override
    public void setScore(@NotNull String teamId, int score) {
    }
    
    /**
     * Set the scores of the given teams
     * @param teamIdsToScores the teamIds to update mapped to their new scores. 
     *                        (Any teamIds not in this TabList will be ignored)
     */
    @Override
    public void setScores(@NotNull Map<@NotNull String, @NotNull Integer> teamIdsToScores) {
    }
    
    /**
     * Set the scores of the given teams
     * @param teams the teams to update the scores of.
     *              (Any teams not in this TabList will be ignored.)
     * @param <T> any implementation of {@link Team}
     */
    @Override
    public <T extends Team> void setScores(Collection<T> teams) {
    }
    
    /**
     * Joins the given participant to the given team so that the name is listed under the team
     * in the TabList. Initialized as alive. <br> 
     * This is not the same as {@link #showPlayer(Player)} because it has nothing
     * to do with who is viewing the TabList. Instead, it has to do with what data is being displayed
     * via the TabList. 
     * @param uuid the participant's uuid
     * @param name the participant's name
     * @param teamId the team to join the participant to
     * @param grey whether the participant's name should be grey, or the color of their team
     */
    @Override
    public void joinParticipant(@NotNull UUID uuid, @NotNull String name, @NotNull String teamId, boolean grey) {
    }
    
    /**
     * Leave the given participant from their team
     * @param uuid the UUID of the participant to leave. Must be a valid UUID contained in this TabList.
     */
    @Override
    public void leaveParticipant(@NotNull UUID uuid) {
    }
    
    /**
     * Set the alive status of the {@link TabList.ParticipantData} associated with the given UUID
     * @param uuid the UUID of the {@link TabList.ParticipantData}
     * @param grey true makes the player name grey, false makes it their team color
     */
    @Override
    public void setParticipantGrey(@NotNull UUID uuid, boolean grey) {
    }
    
    /**
     * Show the given player this TabList
     * @param player the player to view the TabList. Must not already be a viewer.
     */
    @Override
    public void showPlayer(@NotNull Player player) {
    }
    
    /**
     * Players are able to optionally see the TabList content (say, if they want to see the online players list instead, they can hide it). This is not the same as using {@link #showPlayer(Player)} and {@link #hidePlayer(UUID)}, which involves adding/removing players as viewers of this TabList. This merely toggles the content's visibility of players who are viewers of this TabList. 
     * @param uuid the UUID of the player to set the visibility of. Must be the UUID of a player viewing this TabList
     * @param visible true if the player should see the content, false otherwise. 
     */
    @Override
    public void setVisibility(@NotNull UUID uuid, boolean visible) {
    }
    
    /**
     * Hide this TabList from the player with the given UUID
     * @param player the player to hide this TabList from. Must be a player
     *             viewing this TabList. 
     */
    @Override
    public void hidePlayer(@NotNull Player player) {
    }
    
    /**
     * Hide this TabList from the player with the given UUID
     * @param uuid the UUID of the player to hide this TabList from. Must be the UUID of a player
     *             viewing this TabList. 
     */
    @Override
    public void hidePlayer(@NotNull UUID uuid) {
    }
    
    /**
     * Clears the TabList. <br>
     * Remove all teams, remove all participants, remove all viewing players, clear all viewing players'
     * tab headers. 
     */
    @Override
    public void cleanup() {
    }
}
