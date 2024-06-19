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
        private final VersusComponent versusComponent;
        private final String enemyTeam;
        private final List<UUID> members = new ArrayList<>();
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
     * Updates all the given {@link TeamData#getMembers()}' BossBars with the given {@link TeamData#getVersusComponent()}
     * @param teamData the TeamData to update all the members' bossBars. Each member is expected to be a valid key in {@link BattleTopbar#bossBars}. 
     */
    private void updateBossBars(@NotNull TeamData teamData) {
        for (UUID member : teamData.getMembers()) {
            bossBars.get(member).setLeft(teamData.getVersusComponent().toComponent());
        }
    }
    
    /**
     * Make the given player see this BattleTopbar. This also updates the appropriate displays to reflect
     * the new team member and their alive status
     * @param player the player to add to this BattleTopbar
     * @param isAlive whether the player is alive
     * @param teamId the teamId the player is a member of
     */
    public void addPlayer(@NotNull Player player, boolean isAlive, @NotNull String teamId) {
        Preconditions.checkArgument(!bossBars.containsKey(player.getUniqueId()), "player with UUID \"%s\" already exists in this BattleTopbar");
        
        TeamData teamData = teamDatas.get(teamId);
        Preconditions.checkArgument(teamData != null, "team %s does not exist in this BattleTopbar", teamId);
        teamData.getMembers().add(player.getUniqueId());
        teamData.getVersusComponent().getLeft().addMember(isAlive);
        
        TeamData enemyTeamData = teamDatas.get(teamData.getEnemyTeam());
        enemyTeamData.getVersusComponent().getRight().addMember(isAlive);
        
        FormattedBar bossBar = new FormattedBar();
        bossBar.show(player);
        bossBars.put(player.getUniqueId(), bossBar);
        
        updateBossBars(teamData);
        updateBossBars(enemyTeamData);
    }
    
}
