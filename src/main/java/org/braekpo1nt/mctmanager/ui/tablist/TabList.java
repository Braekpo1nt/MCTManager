package org.braekpo1nt.mctmanager.ui.tablist;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.ui.UIException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

public class TabList {
    
    private static final int TEAM_LINE_CHARACTERS = 45;
    private static final int PARTICIPANT_LINE_CHARACTERS = 45;
    
    /**
     * Represents a player who is viewing this TabList
     */
    @Data
    @AllArgsConstructor
    public static class PlayerData {
        private final @NotNull Player player;
        /**
         * True if the TabList should be visible to this player
         * False if the player should not see the TabList
         */
        private boolean visible;
    }
    
    /**
     * Represents a team member, a participant, on a given team
     */
    @Data
    @AllArgsConstructor
    public static class ParticipantData {
        private final @NotNull String name;
        private final @NotNull String teamId;
        private boolean grey;
        
        public Component toTabListEntry(TextColor color, int maxLength) {
            String resultName;
            if (name.length() > maxLength) {
                resultName = name.substring(0, maxLength);
            } else {
                resultName = name;
            }
            if (!grey) {
                return Component.text(resultName).color(color);
            }
            return Component.text(resultName).color(NamedTextColor.DARK_GRAY);
        }
    }
    
    @Data
    @AllArgsConstructor
    public static class TeamData {
        private final @NotNull TextColor color;
        private final @NotNull String name;
        private final @NotNull List<ParticipantData> participants;
        private int score;
        
        public Component toTabListLine(int index) {
            int paddingLength = Math.max(TEAM_LINE_CHARACTERS 
                    - (4 + name.length() + Integer.toString(score).length()), 0);
            return Component.empty()
                    .append(Component.empty()
                            .append(Component.text(String.format("%2d", index)))
                            .append(Component.text(". "))
                            .append(Component.text(name)
                                    .color(color))
                            .append(Component.text(" ".repeat(paddingLength)))
                            .append(Component.text(score)
                                    .color(NamedTextColor.GOLD))
                    )
                    
                    .append(Component.newline())
                    
                    .append(Component.empty()
                            .append(Component.text("     "))
                            .append(getParticipantNamesLine())
                    )
                    ;
        }
        
        private Component getParticipantNamesLine() {
            // alphabetical order
            List<ParticipantData> sortedParticipants = participants.stream().sorted(Comparator.comparing(ParticipantData::getName)).toList();
            List<Integer> nameLengths = sortedParticipants.stream().map(participant -> participant.getName().length()).toList();
            // account for the number of spaces between names
            int maxLineLength = PARTICIPANT_LINE_CHARACTERS - (nameLengths.size() - 1);
            List<Integer> trimLengths = getTrimLengths(nameLengths, maxLineLength);
            TextComponent.Builder builder = Component.text();
            for (int i = 0; i < sortedParticipants.size(); i++) {
                ParticipantData participant = sortedParticipants.get(i);
                builder.append(participant.toTabListEntry(color, trimLengths.get(i)));
                if (i < sortedParticipants.size() - 1) {
                    builder.append(Component.space());
                }
            }
            int totalLength = trimLengths.stream().mapToInt(Integer::intValue).sum() + nameLengths.size() - 1;
            int paddingLength = PARTICIPANT_LINE_CHARACTERS - totalLength;
            builder.append(Component.text(" ".repeat(paddingLength)));
            return builder.build();
        }
    }
    
    /**
     * Figure out how long each name can be and still fit within the given {@code maxLineLength}
     * (spaces between names not included). 
     * @param nameLengths the lengths of the names that are trying to fit in the line
     * @param maxLineLength the maximum characters the line can take up (not including spaces)
     * @return the lengths you must trim each name to. {@code nameLengths.get(n)} should be trimmed to
     * be the length of the {@code n}th result.
     */
    public static @NotNull List<Integer> getTrimLengths(@NotNull List<Integer> nameLengths, int maxLineLength) {
        List<Integer> trimmedLengths = new ArrayList<>(nameLengths);
        if (trimmedLengths.isEmpty()) {
            return trimmedLengths;
        }
        int totalLength = trimmedLengths.stream().mapToInt(Integer::intValue).sum();
        int numOfNames = trimmedLengths.size();
        while (totalLength > maxLineLength) {
            int maxIndex = 0;
            int maxValue = trimmedLengths.getFirst();
            for (int i = 1; i < numOfNames; i++) {
                int value = trimmedLengths.get(i);
                if (value > maxValue) {
                    maxValue = value;
                    maxIndex = i;
                }
            }
            trimmedLengths.set(maxIndex, maxValue - 1);
            totalLength--;
        }
        return trimmedLengths;
    }
    
    private final Map<String, TeamData> teamDatas = new HashMap<>();
    private final Map<UUID, ParticipantData> participantDatas = new HashMap<>();
    private final Map<UUID, PlayerData> playerDatas = new HashMap<>();
    
    private final Main plugin;
    
    public TabList(@NotNull Main plugin) {
        this.plugin = plugin;
    }
    
    /**
     * @param teamId the teamId of the TeamData. Must be a valid key in {@link #teamDatas}
     * @return the {@link TabList.TeamData} associated with this team
     */
    private @Nullable TeamData getTeamData(@NotNull String teamId) {
        TeamData teamData = teamDatas.get(teamId);
        if (teamData == null) {
            logUIError("team %s does not exist in this BattleTopbar", teamId);
        }
        return teamData;
    }
    
    /**
     * @param uuid the UUID of the ParticipantData. Must be a valid key in {@link #participantDatas}
     * @return the {@link TabList.ParticipantData} associated with this UUID
     */
    private @Nullable ParticipantData getParticipantData(@NotNull UUID uuid) {
        ParticipantData participantData = participantDatas.get(uuid);
        if (participantData == null) {
            logUIError("participant with UUID \"%s\" is not contained in this TabList", uuid);
        }
        return participantData;
    }
    
    /**
     * @param playerUUID the UUID of the PlayerData. Must be a valid key in {@link #playerDatas}
     * @return the {@link TabList.PlayerData} associated with this UUID
     */
    private @Nullable PlayerData getPlayerData(@NotNull UUID playerUUID) {
        PlayerData playerData = playerDatas.get(playerUUID);
        if (playerData == null) {
            logUIError("player with UUID \"%s\" does not exist in this BattleTopbar", playerUUID);
        }
        return playerData;
    }
    
    /**
     * @return a playerListHeader Component representing the team data contained in this object
     */
    private Component toTabList() {
        List<TeamData> sortedTeamDatas = teamDatas.values().stream().sorted((team1, team2) -> {
            if (team1.getScore() != team2.getScore()) {
                return Integer.compare(team2.getScore(), team1.getScore());
            }
            return team1.getName().compareTo(team2.getName());
        }).toList();
        TextComponent.Builder builder = Component.text();
        builder.append(Component.text("-".repeat(TEAM_LINE_CHARACTERS))
                .color(NamedTextColor.RED))
                .append(Component.newline());
        builder.append(Component.newline());
        for (int i = 0; i < sortedTeamDatas.size(); i++) {
            TeamData team = sortedTeamDatas.get(i);
            builder
                    .append(team.toTabListLine(i + 1))
                    .append(Component.newline())
                    .append(Component.newline())
            ;
        }
        builder.append(Component.text("-".repeat(TEAM_LINE_CHARACTERS))
                .color(NamedTextColor.RED));
        return builder.asComponent();
    }
    
    /**
     * Updates all views to reflect the current state of the data.
     */
    private void update() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Component tabList = toTabList();
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                for (PlayerData playerData : playerDatas.values()) {
                    playerData.getPlayer().sendPlayerListHeader(tabList);
                }
            });
        });
    }
    
    /**
     * Updates the view to reflect the current state of the data for just
     * the given playerData
     * @param playerData the playerData to update the view of
     */
    private void update(PlayerData playerData) {
        if (playerData.isVisible()) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                Component tabList = toTabList();
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    playerData.getPlayer().sendPlayerListHeader(tabList);
                });
            });
        } else {
            playerData.getPlayer().sendPlayerListHeader(Component.empty());
        }
    }
    
    public void addTeam(@NotNull String teamId, @NotNull String displayName, @NotNull TextColor color) {
        if (teamDatas.containsKey(teamId)) {
            logUIError("Team with teamId \"%s\" already exists in this TabList", teamId);
            return;
        }
        TeamData teamData = new TeamData(color, displayName, new ArrayList<>(), 0);
        teamDatas.put(teamId, teamData);
        update();
    }
    
    public void removeTeam(@NotNull String teamId) {
        TeamData teamData = teamDatas.remove(teamId);
        if (teamData == null) {
            logUIError("Team with teamId \"%s\" does not exist in this TabList", teamId);
            return;
        }
        update();
    }
    
    /**
     * Set the score of the given team
     * @param teamId the teamId to update the score of. Must be a valid teamId in this TabList.
     * @param score the score to update to.
     */
    public void setScore(@NotNull String teamId, int score) {
        TeamData teamData = getTeamData(teamId);
        if (teamData == null) {
            return;
        }
        teamData.setScore(score);
        update();
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
    public void joinParticipant(@NotNull UUID uuid, @NotNull String name, @NotNull String teamId, boolean grey) {
        ParticipantData existingParticipantData = participantDatas.get(uuid);
        if (existingParticipantData != null) {
            logUIError("Participant with UUID \"%s\" and name \"%s\" is already contained in this TabList, joined to team with id \"%s\"", uuid, existingParticipantData.getName(), existingParticipantData.getTeamId());
            return;
        }
        TeamData teamData = getTeamData(teamId);
        if (teamData == null) {
            return;
        }
        ParticipantData newParticipantData = new ParticipantData(name, teamId, grey);
        teamData.getParticipants().add(newParticipantData);
        participantDatas.put(uuid, newParticipantData);
        update();
    }
    
    /**
     * Leave the given participant from their team
     * @param uuid the UUID of the participant to leave. Must be a valid UUID contained in this TabList.
     */
    public void leaveParticipant(@NotNull UUID uuid) {
        ParticipantData participantData = participantDatas.remove(uuid);
        if (participantData == null) {
            logUIError("Participant with UUID \"%s\" is not contained in this TabList", uuid);
            return;
        }
        TeamData teamData = teamDatas.get(participantData.getTeamId());
        teamData.getParticipants().remove(participantData);
        update();
    }
    
    /**
     * Set the alive status of the {@link TabList.ParticipantData} associated with the given UUID
     * @param uuid the UUID of the {@link TabList.ParticipantData}
     * @param grey true makes the player name grey, false makes it their team color
     */
    public void setParticipantGrey(@NotNull UUID uuid, boolean grey) {
        ParticipantData participantData = getParticipantData(uuid);
        if (participantData == null) {
            return;
        }
        participantData.setGrey(grey);
        update();
    }
    
    /**
     * Show the given player this TabList
     * @param player the player to view the TabList. Must not already be a viewer.
     */
    public void showPlayer(@NotNull Player player) {
        if (playerDatas.containsKey(player.getUniqueId())) {
            logUIError("Player with UUID \"%s\" and name \"%s\" is already contained in this TabList", player.getUniqueId(), player.getName());
            return;
        }
        PlayerData playerData = new PlayerData(player, true);
        playerDatas.put(player.getUniqueId(), playerData);
        update(playerData);
    }
    
    /**
     * Players are able to optionally see the TabList content (say, if they want to see the online players list instead, they can hide it). This is not the same as using {@link #showPlayer(Player)} and {@link #hidePlayer(UUID)}, which involves adding/removing players as viewers of this TabList. This merely toggles the content's visibility of players who are viewers of this TabList. 
     * @param uuid the UUID of the player to set the visibility of. Must be the UUID of a player viewing this TabList
     * @param visible true if the player should see the content, false otherwise. 
     */
    public void setVisibility(@NotNull UUID uuid, boolean visible) {
        PlayerData playerData = getPlayerData(uuid);
        if (playerData == null) {
            return;
        }
        playerData.setVisible(visible);
        update(playerData);
    }
    
    /**
     * Hide this TabList from the player with the given UUID
     * @param uuid the UUID of the player to hide this TabList from. Must be the UUID of a player
     *             viewing this TabList. 
     */
    public void hidePlayer(@NotNull UUID uuid) {
        PlayerData playerData = playerDatas.remove(uuid);
        if (playerData == null) {
            logUIError("Player with UUID \"%s\" is not contained in this manager", uuid);
            return;
        }
        playerData.getPlayer().sendPlayerListHeader(Component.empty());
    }
    
    /**
     * Clears the TabList. <br>
     * Remove all teams, remove all participants, remove all viewing players, clear all viewing players'
     * tab headers. 
     */
    public void clear() {
        teamDatas.clear();
        participantDatas.clear();
        for (PlayerData playerData : playerDatas.values()) {
            playerData.getPlayer().sendPlayerListHeader(Component.empty());
        }
        playerDatas.clear();
    }
    
    /**
     * Log a UI error
     * @param reason the reason for the error (a {@link String#format(String, Object...)} template
     * @param args optional args for the reason format string
     */
    private void logUIError(@NotNull String reason, Object... args) {
        Main.logger().log(Level.SEVERE,
                "An error occurred in the ManyBattleTopbar. Failing gracefully.",
                new UIException(String.format(reason, args)));
    }
    
}
