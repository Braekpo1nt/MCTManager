package org.braekpo1nt.mctmanager.ui.topbar;


import com.google.common.base.Preconditions;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * An implementation of a Topbar specifically oriented toward pairs of teams fighting each other.
 * You can add pairs of fighting teams, and add members to them. Then you can add viewers of
 * the Topbar, and each viewer is associated with one of the teams.
 */
public class BattleTopbar {
    
    @Data
    protected static class TeamData {
        /**
         * Holds info about who is dead and alive, and provides an easy way to
         * display that information to the user.
         */
        private final VersusComponent versusComponent;
        /**
         * The enemy team associated with this TeamData. This is useful for
         * updating the displays of both sides of a conflict.
         */
        private final String enemyTeam;
        /**
         * the UUIDs of the players who are viewing this TeamData in their
         * BossBar display. This is useful for updating all appropriate 
         * displays when this TeamData is changed.
         */
        private final List<UUID> viewingMembers = new ArrayList<>();
    }
    
    @Data
    protected static class PlayerData {
        private final @NotNull FormattedBar bossBar;
        private @Nullable String teamId;
    }
    
    /**
     * each team's VersusComponent
     */
    protected final Map<String, TeamData> teamDatas = new HashMap<>();
    protected final Map<UUID, PlayerData> playerDatas = new HashMap<>();
    
    /**
     * @param teamId the teamId of the TeamData. Must be a valid key in {@link BattleTopbar#teamDatas} 
     * @return the {@link TeamData} associated with this team
     * @throws IllegalArgumentException if the teamId is not contained in {@link BattleTopbar#teamDatas}
     */
    private @NotNull TeamData getTeamData(@NotNull String teamId) {
        TeamData teamData = teamDatas.get(teamId);
        Preconditions.checkArgument(teamData != null, "team %s does not exist in this BattleTopbar", teamId);
        return teamData;
    }
    
    /**
     * @param playerUUID the UUID of the PlayerData. Must be a valid key in {@link BattleTopbar#playerDatas}
     * @return the {@link PlayerData} associated with this UUID
     * @throws IllegalArgumentException if the UUID is not contained in {@link BattleTopbar#playerDatas}
     */
    private @NotNull PlayerData getPlayerData(@NotNull UUID playerUUID) {
        PlayerData playerData = playerDatas.get(playerUUID);
        Preconditions.checkArgument(playerData != null, "player with UUID \"%s\" does not exist in this BattleTopbar", playerUUID);
        return playerData;
    }
    
    /**
     * Add a pair of teams to this BattleTopbar. The two teams should be opposing each other.
     * @param teamAId a team involved in the pair
     * @param teamAColor the color of the team
     * @param teamBId another team involved in the pair
     * @param teamBColor the color of the other team
     */
    public void addTeamPair(
            @NotNull String teamAId, @NotNull TextColor teamAColor, 
            @NotNull String teamBId, @NotNull TextColor teamBColor) {
        VersusComponent versusComponentA = new VersusComponent(
                new TeamComponent(0, teamAColor),
                new TeamComponent(0, teamBColor)
        );
        teamDatas.put(teamAId, new TeamData(versusComponentA, teamBId));
        
        VersusComponent versusComponentB = new VersusComponent(
                new TeamComponent(0, teamBColor),
                new TeamComponent(0, teamAColor)
        );
        teamDatas.put(teamBId, new TeamData(versusComponentB, teamAId));
    }
    
    /**
     * Removes all the team pairs from this BattleTopbar
     */
    public void removeAllTeamPairs() {
        teamDatas.clear();
        for (PlayerData playerData : playerDatas.values()) {
            playerData.getBossBar().setLeft(Component.empty());
        }
    }
    
    /**
     * Updates all the given {@link TeamData#getViewingMembers()}' BossBars with the given {@link TeamData#getVersusComponent()}
     * @param teamData the TeamData to update all the members' bossBars. Each member is expected to be a valid key in {@link BattleTopbar#playerDatas}. 
     */
    private void updateBossBars(@NotNull TeamData teamData) {
        for (UUID member : teamData.getViewingMembers()) {
            playerDatas.get(member).getBossBar().setLeft(teamData.getVersusComponent().toComponent());
        }
    }
    
    /**
     * Add a member of the given teamId. Updates all applicable BossBar displays
     * @param teamId the teamId of the team this member belongs to
     * @param living the number of living players on the team
     * @param dead the number of dead players on the team
     */
    public void setMembers(@NotNull String teamId, int living, int dead) {
        Preconditions.checkArgument(living >= 0, "living can't be negative");
        Preconditions.checkArgument(dead >= 0, "dead can't be negative");
        TeamData teamData = getTeamData(teamId);
        teamData.getVersusComponent().getLeft().setMembers(living, dead);
        TeamData enemyTeamData = getTeamData(teamData.getEnemyTeam());
        enemyTeamData.getVersusComponent().getRight().setMembers(living, dead);
        updateBossBars(teamData);
        updateBossBars(enemyTeamData);
    }
    
    /**
     * Make the given player see this BattleTopbar. Please note that this player will start off
     * as not being associated with a teamId. Use {@link BattleTopbar#linkToTeam(UUID, String)}
     * to make the appropriate association.
     * @param player the player to show this BattleTopbar to
     */
    public void showPlayer(@NotNull Player player) {
        Preconditions.checkArgument(!playerDatas.containsKey(player.getUniqueId()), "player with UUID \"%s\" already exists in this BattleTopbar", player.getUniqueId());
        
        FormattedBar bossBar = new FormattedBar(player);
        bossBar.show();
        playerDatas.put(player.getUniqueId(), new PlayerData(bossBar));
    }
    
    /**
     * Link the given player to the given team, so that they are viewing the appropriate
     * components in their BossBar ui and so that they are updated when values relating to 
     * this team are updated. 
     * @param playerUUID the UUID of a player who is viewing this BattleTopbar. Must not already be linked to a teamId in this BattleTopbar.
     * @param teamId the teamId to link this player to (must be a teamId which is already in a teamPair in this BattleTopbar)
     */
    public void linkToTeam(@NotNull UUID playerUUID, @NotNull String teamId) {
        TeamData teamData = getTeamData(teamId);
        PlayerData playerData = getPlayerData(playerUUID);
        Preconditions.checkArgument(playerData.getTeamId() == null, "player with UUID \"%s\" is already linked to a team in this bar: \"%s\"", playerUUID, playerData.getTeamId());
        
        teamData.getViewingMembers().add(playerUUID);
        playerData.setTeamId(teamId);
        
        updateBossBars(teamData);
    }
    
    public void unlinkFromTeam(@NotNull UUID playerUUID, @NotNull String teamId) {
        TeamData teamData = getTeamData(teamId);
        PlayerData playerData = getPlayerData(playerUUID);
        Preconditions.checkArgument(playerData.getTeamId() != null, "player with UUID \"%s\" is not linked to any team", playerUUID);
        Preconditions.checkArgument(playerData.getTeamId().equals(teamId), "player with UUID \"%s\" is not linked to teamId \"%s\", but instead are linked to \"%s\"", playerUUID, teamId, playerData.getTeamId());
        
        teamData.getViewingMembers().remove(playerUUID);
        playerData.setTeamId(teamId);
    }
    
    /**
     * Make the given player no longer see this BattleTopbar
     * @param playerUUID the UUID of the player to hide
     */
    public void hidePlayer(@NotNull UUID playerUUID) {
        PlayerData playerData = playerDatas.remove(playerUUID);
        Preconditions.checkArgument(playerData != null, "player with UUID \"%s\" does not exist in this BattleTopbar", playerUUID);
        if (playerData.getTeamId() != null) {
            TeamData teamData = getTeamData(playerData.getTeamId());
            teamData.getViewingMembers().remove(playerUUID);
        }
        playerData.getBossBar().hide();
    }
    
    /**
     * A bulk operation version of {@link BattleTopbar#hidePlayer(UUID)}
     * @param playerUUIDs a List of the UUIDs of each player to remove
     */
    public void hidePlayers(@NotNull List<UUID> playerUUIDs) {
        for (UUID playerUUID : playerUUIDs) {
            hidePlayer(playerUUID);
        }
    }
    
    public void hideAllPlayers() {
        for (PlayerData playerData : playerDatas.values()) {
            playerData.getBossBar().hide();
        }
        playerDatas.clear();
    }
    
    /**
     * Set the left display of the BattleTopbar to the given component
     * @param left the component to set the left section to
     */
    public void setLeft(@NotNull Component left) {
        for (PlayerData playerData : playerDatas.values()) {
            playerData.getBossBar().setLeft(left);
        }
    }
    
    /**
     * Set the left display of the BattleTopbar to the given component, specifically for one player
     * @param playerUUID the player to set the left section for
     * @param left the component to set the left section to
     */
    public void setLeft(@NotNull UUID playerUUID, @NotNull Component left) {
        PlayerData playerData = getPlayerData(playerUUID);
        playerData.getBossBar().setLeft(left);
    }
    
    /**
     * Set the middle display of the BattleTopbar to the given component
     * @param middle the component to set the middle section to
     */
    public void setMiddle(@NotNull Component middle) {
        for (PlayerData playerData : playerDatas.values()) {
            playerData.getBossBar().setMiddle(middle);
        }
    }
    
    /**
     * Set the middle display of the BattleTopbar to the given component, specifically for one player
     * @param playerUUID the player to set the middle section for
     * @param middle the component to set the middle section to
     */
    public void setMiddle(@NotNull UUID playerUUID, @NotNull Component middle) {
        PlayerData playerData = getPlayerData(playerUUID);
        playerData.getBossBar().setMiddle(middle);
    }
    
    /**
     * 
     * @param playerUUID the player to set the kills for
     * @param kills the number of kills
     */
    public void setKills(@NotNull UUID playerUUID, int kills) {
        PlayerData playerData = getPlayerData(playerUUID);
        playerData.getBossBar().setRight(Component.empty()
                .append(Component.text("K: "))
                .append(Component.text(kills))
        );
    }
    
}
