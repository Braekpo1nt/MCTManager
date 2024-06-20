package org.braekpo1nt.mctmanager.ui.topbar;


import com.google.common.base.Preconditions;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
        private final @NotNull String teamId;
        private int kills;
    }
    
    /**
     * each team's VersusComponent
     */
    protected final Map<String, TeamData> teamDatas = new HashMap<>();
    protected final Map<UUID, PlayerData> playerDatas = new HashMap<>();
    
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
     * Update all appropriate BossBar displays with the death of a member of the given teamId
     * @param teamId the teamId of the player who died
     * @throws IllegalArgumentException if the given teamId is not already contained in this BattleTopbar
     */
    public void addDeath(@NotNull String teamId) {
        TeamData teamData = teamDatas.get(teamId);
        Preconditions.checkArgument(teamData != null, "team \"%s\" is not in this BattleTopbar", teamId);
        TeamData enemyTeamData = teamDatas.get(teamData.getEnemyTeam());
        teamData.getVersusComponent().getLeft().addDeaths(1);
        enemyTeamData.getVersusComponent().getRight().addDeaths(1);
        updateBossBars(teamData);
        updateBossBars(enemyTeamData);
    }
    
    /**
     * Update all appropriate BossBar displays with the life of a member of the given teamId
     * @param teamId the teamId of the player who is no longer dead
     */
    public void removeDeath(@NotNull String teamId) {
        TeamData teamData = teamDatas.get(teamId);
        Preconditions.checkArgument(teamData != null, "team \"%s\" is not in this BattleTopbar", teamId);
        TeamData enemyTeamData = teamDatas.get(teamData.getEnemyTeam());
        teamData.getVersusComponent().getLeft().addLiving(1);
        enemyTeamData.getVersusComponent().getRight().addLiving(1);
        updateBossBars(teamData);
        updateBossBars(enemyTeamData);
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
     * @param isAlive whether the new member is alive
     * @param teamId the teamId of the team this member belongs to
     */
    public void addMember(boolean isAlive, @NotNull String teamId) {
        TeamData teamData = teamDatas.get(teamId);
        Preconditions.checkArgument(teamData != null, "team %s does not exist in this BattleTopbar", teamId);
        teamData.getVersusComponent().getLeft().addMember(isAlive);
        TeamData enemyTeamData = teamDatas.get(teamData.getEnemyTeam());
        enemyTeamData.getVersusComponent().getRight().addMember(isAlive);
        updateBossBars(teamData);
        updateBossBars(enemyTeamData);
    }
    
    /**
     * Remove a member of the given teamId. Updates all applicable BossBar displays
     * @param isAlive whether the new member is alive
     * @param teamId the teamId of the team this member belongs to
     */
    public void removeMember(boolean isAlive, @NotNull String teamId) {
        TeamData teamData = teamDatas.get(teamId);
        Preconditions.checkArgument(teamData != null, "team %s does not exist in this BattleTopbar", teamId);
        teamData.getVersusComponent().getLeft().removeMember(isAlive);
        TeamData enemyTeamData = teamDatas.get(teamData.getEnemyTeam());
        enemyTeamData.getVersusComponent().getRight().removeMember(isAlive);
        updateBossBars(teamData);
        updateBossBars(enemyTeamData);
    }
    
    /**
     * Make the given player see this BattleTopbar 
     * @param player the player to show this BattleTopbar to
     * @param teamId the teamId the player is a member of
     */
    public void showPlayer(@NotNull Player player, @NotNull String teamId) {
        Preconditions.checkArgument(!playerDatas.containsKey(player.getUniqueId()), "player with UUID \"%s\" already exists in this BattleTopbar", player.getUniqueId());
        
        TeamData teamData = teamDatas.get(teamId);
        Preconditions.checkArgument(teamData != null, "team %s does not exist in this BattleTopbar", teamId);
        teamData.getViewingMembers().add(player.getUniqueId());
        
        FormattedBar bossBar = new FormattedBar(player);
        bossBar.show();
        playerDatas.put(player.getUniqueId(), new PlayerData(bossBar, teamId));
        
        updateBossBars(teamData);
    }
    
    /**
     * Make the given player no longer see this BattleTopbar
     * @param playerUUID the UUID of the player to hide
     */
    public void hidePlayer(@NotNull UUID playerUUID) {
        PlayerData playerData = playerDatas.remove(playerUUID);
        Preconditions.checkArgument(playerData != null, "player with UUID \"%s\" does not exist in this BattleTopbar", playerUUID);
        TeamData teamData = teamDatas.get(playerData.getTeamId());
        Preconditions.checkArgument(teamData != null, "team %s does not exist in this BattleTopbar", playerData.getTeamId());
        teamData.getViewingMembers().remove(playerUUID);
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
        PlayerData playerData = playerDatas.get(playerUUID);
        Preconditions.checkArgument(playerData != null, "player with UUID \"%s\" does not exist in this BattleTopbar", playerUUID);
        playerData.getBossBar().setMiddle(middle);
    }
    
    /**
     * 
     * @param playerUUID the player to set the kills for
     * @param kills the number of kills
     */
    public void setKills(@NotNull UUID playerUUID, int kills) {
        PlayerData playerData = playerDatas.get(playerUUID);
        Preconditions.checkArgument(playerData != null, "player with UUID \"%s\" does not exist in this BattleTopbar", playerUUID);
        playerData.setKills(kills);
        setRight(Component.empty()
                .append(Component.text("K: "))
                .append(Component.text(kills))
        );
    }
    
    /**
     * Set the right display of the BattleTopbar to the given component
     * @param right the component to set the right section to
     */
    public void setRight(@NotNull Component right) {
        for (PlayerData playerData : playerDatas.values()) {
            playerData.getBossBar().setRight(right);
        }
    }
    
    /**
     * Set the right display of the BattleTopbar to the given component, specifically for one player
     * @param playerUUID the player to set the right section for
     * @param right the component to set the right section to
     */
    public void setRight(@NotNull UUID playerUUID, @NotNull Component right) {
        PlayerData playerData = playerDatas.get(playerUUID);
        Preconditions.checkArgument(playerData != null, "player with UUID \"%s\" does not exist in this BattleTopbar", playerUUID);
        playerData.getBossBar().setRight(right);
    }
    
}
