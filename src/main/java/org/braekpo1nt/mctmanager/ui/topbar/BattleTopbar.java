package org.braekpo1nt.mctmanager.ui.topbar;


import com.google.common.base.Preconditions;
import lombok.Data;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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
    
    /**
     * each team's VersusComponent
     */
    protected final Map<String, TeamData> teamDatas = new HashMap<>();
    protected final Map<UUID, FormattedBar> bossBars = new HashMap<>();
    
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
     * @param teamData the TeamData to update all the members' bossBars. Each member is expected to be a valid key in {@link BattleTopbar#bossBars}. 
     */
    private void updateBossBars(@NotNull TeamData teamData) {
        for (UUID member : teamData.getViewingMembers()) {
            bossBars.get(member).setLeft(teamData.getVersusComponent().toComponent());
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
        Preconditions.checkArgument(!bossBars.containsKey(player.getUniqueId()), "player with UUID \"%s\" already exists in this BattleTopbar", player.getUniqueId());
        
        TeamData teamData = teamDatas.get(teamId);
        Preconditions.checkArgument(teamData != null, "team %s does not exist in this BattleTopbar", teamId);
        teamData.getViewingMembers().add(player.getUniqueId());
        
        FormattedBar bossBar = new FormattedBar();
        bossBar.show(player);
        bossBars.put(player.getUniqueId(), bossBar);
        
        updateBossBars(teamData);
    }
    
    /**
     * Make the given player no longer see this BattleTopbar
     * @param player the player to hide
     * @param teamId the teamId the player is a member of
     */
    public void hidePlayer(@NotNull Player player, @NotNull String teamId) {
        FormattedBar bossBar = bossBars.remove(player.getUniqueId());
        Preconditions.checkArgument(bossBar != null, "player with UUID \"%s\" does not exist in this BattleTopbar", player.getUniqueId());
        TeamData teamData = teamDatas.get(teamId);
        Preconditions.checkArgument(teamData != null, "team %s does not exist in this BattleTopbar", teamId);
        teamData.getViewingMembers().remove(player.getUniqueId());
        bossBar.hide(player);
    }
    
    /**
     * A bulk operation version of {@link BattleTopbar#hidePlayer(Player, String)}
     * @param playersToTeams a map of each player to remove to the teamId of the team they are a member of
     */
    public void hidePlayers(@NotNull Map<Player, String> playersToTeams) {
        for (Map.Entry<Player, String> entry : playersToTeams.entrySet()) {
            hidePlayer(entry.getKey(), entry.getValue());
        }
    }
    
}
