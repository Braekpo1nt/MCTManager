package org.braekpo1nt.mctmanager.ui.topbar;


import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.ui.topbar.components.KillDeathComponent;
import org.braekpo1nt.mctmanager.ui.topbar.components.TeamComponent;
import org.braekpo1nt.mctmanager.ui.topbar.components.VersusComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * An implementation of a Topbar specifically oriented toward pairs of teams fighting each other.
 * You can add pairs of fighting teams, and add members to them. Then you can add viewers of
 * the Topbar, and each viewer is associated with one of the teams.
 */
public class BattleTopbar implements Topbar {
    
    @Data
    private static class TeamData {
        /**
         * Holds info about who is dead and alive, and provides an easy way to
         * display that information to the user.
         */
        private @NotNull final VersusComponent versusComponent;
        /**
         * The enemy team associated with this TeamData. This is useful for
         * updating the displays of both sides of a conflict.
         * If this is null, there is no enemy team associated with this team. Use {@link BattleTopbar#linkTeamPair(String, String)} to link with a team.
         */
        private @Nullable String enemyTeam;
        private final @NotNull TextColor teamColor;
        /**
         * the UUIDs of the players who are viewing this TeamData in their
         * BossBar display. This is useful for updating all appropriate 
         * displays when this TeamData is changed.
         */
        private final List<UUID> viewingMembers = new ArrayList<>();
    }
    
    @Data
    private static class PlayerData {
        private final @NotNull FormattedBar bossBar;
        private @Nullable String teamId;
        private @Nullable KillDeathComponent killDeathComponent;
    }
    
    /**
     * each team's TeamData
     */
    private final Map<String, TeamData> teamDatas = new HashMap<>();
    /**
     * Each player's PlayerData
     */
    private final Map<UUID, PlayerData> playerDatas = new HashMap<>();
    /**
     * the component to use as the default left section of the BossBar display
     * if the viewing player is not linked to a team. 
     * @see BattleTopbar#linkToTeam(UUID, String) 
     * @see BattleTopbar#unlinkFromTeam(UUID) 
     */
    protected @NotNull Component noTeamLeft;
    
    public BattleTopbar() {
        this.noTeamLeft = Component.empty();
    }
    
    /**
     * @param noTeamLeft the component to use as the default left section of the 
     *                   BossBar display if the viewing player is not linked to a team.
     */
    public BattleTopbar(@NotNull Component noTeamLeft) {
        this.noTeamLeft = noTeamLeft;
    }
    
    /**
     * @param noTeamLeft the component to use as the default left section of the 
     *                   BossBar display if the viewing player is not linked to a team.
     */
    public void setNoTeamLeft(@NotNull Component noTeamLeft) {
        this.noTeamLeft = noTeamLeft;
        for (PlayerData playerData : playerDatas.values()) {
            update(playerData);
        }
    }
    
    /**
     * @param teamId the teamId of the TeamData. Must be a valid key in {@link BattleTopbar#teamDatas} 
     * @return the {@link TeamData} associated with this team
     */
    private @Nullable TeamData getTeamData(@NotNull String teamId) {
        TeamData teamData = teamDatas.get(teamId);
        if (teamData == null) {
            logUIError("team %s does not exist in this BattleTopbar", teamId);
        }
        return teamData;
    }
    
    /**
     * @param playerUUID the UUID of the PlayerData. Must be a valid key in {@link BattleTopbar#playerDatas}
     * @return the {@link PlayerData} associated with this UUID
     */
    private @Nullable PlayerData getPlayerData(@NotNull UUID playerUUID) {
        PlayerData playerData = playerDatas.get(playerUUID);
        if (playerData == null) {
            logUIError("player with UUID \"%s\" does not exist in this BattleTopbar", playerUUID);
        }
        return playerData;
    }
    
    /**
     * Add a pair of teams to this BattleTopbar. The two teams should be opposing each other.
     * @param teamId a team involved in the pair
     * @param teamColor the color of the team
     */
    public void addTeam(
            @NotNull String teamId, @NotNull TextColor teamColor) {
        teamDatas.put(teamId, new TeamData(new VersusComponent(new TeamComponent(teamColor)), teamColor));
    }
    
    /**
     * set two teams to be opposing each other. Must be different teams. Both teams
     * must have been added to this Topbar before the link operation 
     * using {@link BattleTopbar#addTeam(String, TextColor)}
     * @param teamIdA a valid teamId in this Topbar
     * @param teamIdB another valid teamId in this Topbar
     * @see BattleTopbar#addTeam(String, TextColor)
     */
    public void linkTeamPair(@NotNull String teamIdA, @NotNull String teamIdB) {
        if (teamIdA.equals(teamIdB)) {
            logUIError("teamIdA can't be equal to teamIdB (%s)", teamIdA);
            return;
        }
        TeamData teamDataA = getTeamData(teamIdA);
        if (teamDataA == null) {
            return;
        }
        TeamData teamDataB = getTeamData(teamIdB);
        if (teamDataB == null) {
            return;
        }
        
        teamDataA.setEnemyTeam(teamIdB);
        teamDataA.getVersusComponent().setRight(
                new TeamComponent(teamDataB.getTeamColor())
        );
        
        teamDataB.setEnemyTeam(teamIdA);
        teamDataB.getVersusComponent().setRight(
                new TeamComponent(teamDataA.getTeamColor())
        );
        
        update(teamDataA);
        update(teamDataB);
    }
    
    /**
     * The opposite operation of {@link BattleTopbar#linkTeamPair(String, String)}. Unlinks
     * two teams from each other.
     * @param teamIdA a valid teamId in this Topbar, which is linked to teamIdB
     * @param teamIdB a valid teamId in this Topbar, which is linked to teamIdA
     */
    public void unlinkTeamPair(@NotNull String teamIdA, @NotNull String teamIdB) {
        if (teamIdA.equals(teamIdB)) {
            logUIError("teamIdA can't be equal to teamIdB (%s)", teamIdA);
            return;
        }
        TeamData teamDataA = getTeamData(teamIdA);
        if (teamDataA == null) {
            return;
        }
        TeamData teamDataB = getTeamData(teamIdB);
        if (teamDataB == null) {
            return;
        }
        if (teamDataA.getEnemyTeam() == null) {
            logUIError("%s is not linked to any team", teamIdA);
            return;
        }
        if (!teamDataA.getEnemyTeam().equals(teamIdB)) {
            logUIError("%s is not linked to %s", teamDataA, teamDataB);
            return;
        }
        if (teamDataB.getEnemyTeam() == null) {
            logUIError("%s is not linked to any team", teamIdB);
            return;
        }
        if (!teamDataB.getEnemyTeam().equals(teamIdA)) {
            logUIError("%s is not linked to %s", teamDataB, teamDataA);
            return;
        }
        
        teamDataA.setEnemyTeam(null);
        teamDataA.getVersusComponent().setRight(null);
        teamDataB.setEnemyTeam(null);
        teamDataB.getVersusComponent().setRight(null);
        
        update(teamDataA);
        update(teamDataB);
    }
    
    /**
     * Removes all the team pairs from this Topbar, and unlinks all
     * players from their teamIds
     */
    public void removeAllTeamPairs() {
        teamDatas.clear();
        for (PlayerData playerData : playerDatas.values()) {
            playerData.setTeamId(null);
            update(playerData);
        }
    }
    
    /**
     * Updates all the given {@link TeamData#getViewingMembers()}' BossBars with the given {@link TeamData#getVersusComponent()}
     * @param teamData the TeamData to update all the members' bossBars. Each member is expected to be a valid key in {@link BattleTopbar#playerDatas}. 
     */
    private void update(@NotNull TeamData teamData) {
        for (UUID member : teamData.getViewingMembers()) {
            PlayerData playerData = getPlayerData(member);
            if (playerData != null) {
                playerData.getBossBar().setLeft(teamData.getVersusComponent().toComponent());
            }
        }
    }
    
    /**
     * Updates the display BossBar for the given PlayerData. If the player
     * is on a team, they will see the appropriate TeamData. Otherwise, they
     * will see noTeamData
     * @param playerData the PlayerData to update
     */
    private void update(@NotNull PlayerData playerData) {
        if (playerData.getTeamId() == null) {
            playerData.getBossBar().setLeft(noTeamLeft);
            return;
        }
        TeamData teamData = getTeamData(playerData.getTeamId());
        if (teamData == null) {
            return;
        }
        playerData.getBossBar().setLeft(teamData.getVersusComponent().toComponent());
    }
    
    /**
     * Set the number of living and dead players on a team. Updates all applicable BossBar displays
     * @param teamId the teamId of the team this member belongs to
     * @param living the number of living players on the team
     * @param dead the number of dead players on the team
     */
    public void setMembers(@NotNull String teamId, int living, int dead) {
        if (living < 0) {
            logUIError("living can't be negative");
            return;
        }
        if (dead < 0) {
            logUIError("dead can't be negative");
            return;
        }
        TeamData teamData = getTeamData(teamId);
        if (teamData == null) {
            return;
        }
        teamData.getVersusComponent().getLeft().setMembers(living, dead);
        update(teamData);
        if (teamData.getEnemyTeam() != null) {
            TeamData enemyTeamData = getTeamData(teamData.getEnemyTeam());
            if (enemyTeamData != null && enemyTeamData.getVersusComponent().getRight() != null) {
                enemyTeamData.getVersusComponent().getRight().setMembers(living, dead);
                update(enemyTeamData);
            }
        }
    }
    
    /**
     * Make the given player see this Topbar. Please note that this player will start off
     * as not being associated with a teamId. Use {@link BattleTopbar#linkToTeam(UUID, String)}
     * to make the appropriate association.
     * @param player the player to show this Topbar to
     */
    public void showPlayer(@NotNull Player player) {
        if (playerDatas.containsKey(player.getUniqueId())) {
            logUIError("player with UUID \"%s\" already exists in this BattleTopbar", player.getUniqueId());
            return;
        }
        
        FormattedBar bossBar = new FormattedBar(player);
        bossBar.show();
        PlayerData playerData = new PlayerData(bossBar);
        playerDatas.put(player.getUniqueId(), playerData);
        update(playerData);
    }
    
    /**
     * Link the given player to the given team, so that they are viewing the appropriate
     * components in their BossBar ui and so that they are updated when values relating to 
     * this team are updated. 
     * @param playerUUID the UUID of a player who is viewing this BattleTopbar. Must not already be linked to a teamId in this BattleTopbar.
     * @param teamId the teamId to link this player to (must be a teamId which is already in this Topbar)
     */
    public void linkToTeam(@NotNull UUID playerUUID, @NotNull String teamId) {
        TeamData teamData = getTeamData(teamId);
        if (teamData == null) {
            return;
        }
        PlayerData playerData = getPlayerData(playerUUID);
        if (playerData == null) {
            return;
        }
        if (playerData.getTeamId() != null) {
            logUIError("player with UUID \"%s\" is already linked to a team in this bar: \"%s\"", 
                    playerUUID, playerData.getTeamId());
            return;
        }
        
        teamData.getViewingMembers().add(playerUUID);
        playerData.setTeamId(teamId);
        
        update(playerData);
    }
    
    /**
     * Unlink the given player from their team. 
     * @param playerUUID the UUID of the player to remove this. Must already be linked to a teamId in this Topbar.
     * @throws IllegalArgumentException if they are not already linked to a team, or if they are not in this Topbar
     */
    public void unlinkFromTeam(@NotNull UUID playerUUID) {
        PlayerData playerData = getPlayerData(playerUUID);
        if (playerData == null) {
            return;
        }
        if (playerData.getTeamId() == null) {
            logUIError("player with UUID \"%s\" is not linked to any team", playerUUID);
            return;
        }
        String teamId = playerData.getTeamId();
        TeamData teamData = getTeamData(teamId);
        if (teamData == null) {
            return;
        }
        
        teamData.getViewingMembers().remove(playerUUID);
        playerData.setTeamId(teamId);
        
        update(playerData);
    }
    
    /**
     * Make the given player no longer see this BattleTopbar
     * @param playerUUID the UUID of the player to hide
     */
    public void hidePlayer(@NotNull UUID playerUUID) {
        PlayerData playerData = playerDatas.remove(playerUUID);
        if (playerData == null) {
            logUIError("player with UUID \"%s\" does not exist in this BattleTopbar", playerUUID);
            return;
        }
        if (playerData.getTeamId() != null) {
            TeamData teamData = getTeamData(playerData.getTeamId());
            if (teamData != null) {
                teamData.getViewingMembers().remove(playerUUID);
            }
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
     * {@inheritDoc}
     */
    @Override
    public void setLeft(@NotNull Component left) {
        for (PlayerData playerData : playerDatas.values()) {
            playerData.getBossBar().setLeft(left);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setLeft(@NotNull UUID playerUUID, @NotNull Component left) {
        PlayerData playerData = getPlayerData(playerUUID);
        if (playerData == null) {
            return;
        }
        playerData.getBossBar().setLeft(left);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setMiddle(@NotNull Component middle) {
        for (PlayerData playerData : playerDatas.values()) {
            playerData.getBossBar().setMiddle(middle);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setMiddle(@NotNull UUID playerUUID, @NotNull Component middle) {
        PlayerData playerData = getPlayerData(playerUUID);
        if (playerData == null) {
            return;
        }
        playerData.getBossBar().setMiddle(middle);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setRight(@NotNull Component right) {
        for (PlayerData playerData : playerDatas.values()) {
            playerData.getBossBar().setRight(right);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setRight(@NotNull UUID playerUUID, @NotNull Component right) {
        PlayerData playerData = getPlayerData(playerUUID);
        if (playerData == null) {
            return;
        }
        playerData.getBossBar().setRight(right);
    }
    
    /**
     * @param playerUUID the player to set the kills for
     * @param kills the number of kills
     */
    public void setKills(@NotNull UUID playerUUID, int kills) {
        PlayerData playerData = getPlayerData(playerUUID);
        if (playerData == null) {
            return;
        }
        if (playerData.getKillDeathComponent() == null) {
            playerData.setKillDeathComponent(new KillDeathComponent());
        }
        playerData.getKillDeathComponent().setKills(kills);
        playerData.getBossBar().setRight(playerData.getKillDeathComponent().toComponent());
    }
    
    /**
     * @param playerUUID the player to set the deaths for
     * @param deaths the number of deaths
     */
    public void setDeaths(@NotNull UUID playerUUID, int deaths) {
        PlayerData playerData = getPlayerData(playerUUID);
        if (playerData == null) {
            return;
        }
        if (playerData.getKillDeathComponent() == null) {
            playerData.setKillDeathComponent(new KillDeathComponent());
        }
        playerData.getKillDeathComponent().setDeaths(deaths);
        playerData.getBossBar().setRight(playerData.getKillDeathComponent().toComponent());
    }
    
    /**
     * @param playerUUID the player to set the kills and deaths for
     * @param kills the number of kills
     * @param deaths the number of deaths
     */
    public void setKillsAndDeaths(@NotNull UUID playerUUID, int kills, int deaths) {
        PlayerData playerData = getPlayerData(playerUUID);
        if (playerData == null) {
            return;
        }
        if (playerData.getKillDeathComponent() == null) {
            playerData.setKillDeathComponent(new KillDeathComponent());
        }
        playerData.getKillDeathComponent().setKills(kills);
        playerData.getKillDeathComponent().setDeaths(deaths);
        playerData.getBossBar().setRight(playerData.getKillDeathComponent().toComponent());
    }
    
    /**
     * Log a UI error
     * @param reason the reason for the error (a {@link String#format(String, Object...)} template
     * @param args optional args for the reason format string
     */
    private void logUIError(@NotNull String reason, Object... args) {
        Main.logger().severe(String.format(reason, args));
    }
    
}
