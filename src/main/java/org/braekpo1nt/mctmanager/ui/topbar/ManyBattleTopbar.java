package org.braekpo1nt.mctmanager.ui.topbar;

import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.topbar.components.KillDeathComponent;
import org.braekpo1nt.mctmanager.ui.topbar.components.ManyTeamsComponent;
import org.braekpo1nt.mctmanager.ui.topbar.components.TeamComponent;
import org.braekpo1nt.mctmanager.ui.topbar.components.VersusManyComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * An implementation of a Topbar specifically oriented towards many teams fighting each other.
 * You can add teams, and add members to them. Then you can add viewers of the Topbar,
 * and each viewer is associated with one of the teams.
 */
public class ManyBattleTopbar implements Topbar {
    
    public ManyBattleTopbar() {
        allTeams = new ManyTeamsComponent();
    }
    
    @Data
    protected static class TeamData {
        private final VersusManyComponent versusManyComponent;
        /**
         * how many players are alive on this team
         */
        private int aliveCount = 0;
        private final TextColor color;
        private final List<UUID> viewingMembers = new ArrayList<>();
    }
    
    @Data
    protected static class PlayerData {
        private final @NotNull FormattedBar bossBar;
        private @Nullable String teamId;
        private @Nullable KillDeathComponent killDeathComponent;
    }
    
    /**
     * each team's TeamData
     */
    private final Map<String, TeamData> teamDatas = new HashMap<>();
    /**
     * each player's PlayerData
     */
    private final Map<UUID, PlayerData> playerDatas = new HashMap<>();
    /**
     * A component with all the teams and their status. Used for displaying to players
     * who are not a member of a team, and thus should see a summary of all teams
     * (such as admins and spectators). 
     */
    private final ManyTeamsComponent allTeams;
    
    /**
     * @param teamId the teamId of the TeamData. Must be a valid key in {@link ManyBattleTopbar#teamDatas}
     * @return the {@link ManyBattleTopbar.TeamData} associated with this team
     */
    private @Nullable TeamData getTeamData(@NotNull String teamId) {
        TeamData teamData = teamDatas.get(teamId);
        if (teamData == null) {
            UIUtils.logUIError("team %s does not exist in this BattleTopbar", teamId);
        }
        return teamData;
    }
    
    /**
     * @param playerUUID the UUID of the PlayerData. Must be a valid key in {@link ManyBattleTopbar#playerDatas}
     * @return the {@link ManyBattleTopbar.PlayerData} associated with this UUID
     */
    private @Nullable PlayerData getPlayerData(@NotNull UUID playerUUID) {
        PlayerData playerData = playerDatas.get(playerUUID);
        if (playerData == null) {
            UIUtils.logUIError("player with UUID \"%s\" does not exist in this BattleTopbar", playerUUID);
        }
        return playerData;
    }
    
    /**
     * To be called when the number of living or dead members changes on any team.
     * Updates all views to reflect the new number of living members on the specific team.
     */
    private void update() {
        for (PlayerData playerData : playerDatas.values()) {
            update(playerData);
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
            playerData.getBossBar().setLeft(allTeams.toComponent());
            return;
        }
        TeamData teamData = getTeamData(playerData.getTeamId());
        if (teamData == null) {
            return;
        }
        playerData.getBossBar().setLeft(teamData.getVersusManyComponent().toComponent());
    }
    
    /**
     * Add a new team to this Topbar. Updates appropriate BossBar displays. 
     * @param teamId the teamId of the team to add
     */
    public void addTeam(@NotNull String teamId, @NotNull TextColor teamColor) {
        TeamData newTeamData = new TeamData(new VersusManyComponent(new TeamComponent(teamColor)), teamColor);
        teamDatas.put(teamId, newTeamData);
        for (Map.Entry<String, TeamData> entry : teamDatas.entrySet()) {
            String opponentTeamId = entry.getKey();
            if (!opponentTeamId.equals(teamId)) {
                TeamData teamData = entry.getValue();
                teamData.getVersusManyComponent().getOpponents().addTeam(teamId, teamColor);
                newTeamData.getVersusManyComponent().getOpponents().addTeam(opponentTeamId, teamData.getColor());
                newTeamData.getVersusManyComponent().getOpponents().setAliveCount(opponentTeamId, teamData.getAliveCount());
            }
        }
        allTeams.addTeam(teamId, teamColor);
        update();
    }
    
    /**
     * Removes all the teams from this Topbar, and unlinks all players from 
     * their teamIds
     * @deprecated use {@link #clear()}
     */
    @Deprecated
    public void removeAllTeams() {
        teamDatas.clear();
        for (PlayerData playerData : playerDatas.values()) {
            playerData.setTeamId(null);
            update(playerData);
        }
    }
    
    /**
     * Set the number of living and dead players on a team. The members of that team will see the left part of the {@link VersusManyComponent} updated, and all other viewers will see the right part updated. 
     * @param teamId the teamId to set the living/dead members for
     * @param living the number of living players on the team
     * @param dead the number of dead players on the team
     */
    public void setMembers(@NotNull String teamId, int living, int dead) {
        if (living < 0) {
            UIUtils.logUIError("living can't be negative");
            return;
        }
        TeamData teamData = getTeamData(teamId);
        if (teamData == null) {
            return;
        }
        teamData.getVersusManyComponent().getFriendly().setMembers(living, dead);
        teamData.setAliveCount(living);
        for (Map.Entry<String, TeamData> entry : teamDatas.entrySet()) {
            if (!entry.getKey().equals(teamId)) {
                TeamData eachTeamData = entry.getValue();
                eachTeamData.getVersusManyComponent()
                        .getOpponents().setAliveCount(teamId, living);
            }
        }
        allTeams.setAliveCount(teamId, living);
        update();
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
            UIUtils.logUIError("player with UUID \"%s\" is already linked to a team in this bar: \"%s\"", playerUUID, playerData.getTeamId());
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
            UIUtils.logUIError("player with UUID \"%s\" is not linked to any team", playerUUID);
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
     * Make the given player see this Topbar. Please note that this player will start off
     * as not being associated with a teamId. Use {@link ManyBattleTopbar#linkToTeam(UUID, String)}
     * to make the appropriate association.
     * @param player the player to show this Topbar to
     */
    public void showPlayer(@NotNull Player player) {
        if (playerDatas.containsKey(player.getUniqueId())) {
            UIUtils.logUIError("player with UUID \"%s\" already exists in this ManyBattleTopbar", player.getUniqueId());
            return;
        }
        FormattedBar bossBar = new FormattedBar(player);
        bossBar.show();
        PlayerData playerData = new PlayerData(bossBar);
        playerDatas.put(player.getUniqueId(), playerData);
        update(playerData);
    }
    
    @Override
    public void clear() {
        hideAllPlayers();
        removeAllTeams();
    }
    
    /**
     * Make the given player no longer see this BattleTopbar
     * @param playerUUID the UUID of the player to hide
     */
    public void hidePlayer(@NotNull UUID playerUUID) {
        PlayerData playerData = playerDatas.remove(playerUUID);
        if (playerData == null) {
            UIUtils.logUIError("player with UUID \"%s\" does not exist in this BattleTopbar", playerUUID);
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
    
    /**
     * @deprecated use {@link #clear()}
     */
    @Deprecated
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
     * @param deaths the number of deaths. Negative numbers will result in no
     *               deaths being shown. 
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
}
