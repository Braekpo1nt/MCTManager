package org.braekpo1nt.mctmanager.games.gamestate;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Logger;

/**
 * Handles the CRUD operations for storing GameState objects
 * keeps the most recently loaded GameState in memory so that each call to it doesn't need to be a file IO operation
 */
public class GameStateStorageUtil {
    
    private final Logger LOGGER;
    private final GameStateController gameStateController;
    protected GameState gameState = new GameState(new HashMap<>(), new HashMap<>(), new HashMap<>(), new ArrayList<>());
    
    public GameStateStorageUtil(Main plugin) {
        this.LOGGER = plugin.getLogger();
        this.gameStateController = new GameStateController(plugin.getDataFolder());
    }
    
    /**
     * Save the GameState to storage
     * @throws ConfigIOException if there is a problem
     * - creating a new game state file
     * - writing to the game state file
     * - converting the game state to json
     */
    public void saveGameState() throws ConfigIOException {
        gameStateController.saveGameState(gameState);
    }
    
    /**
     * Load the GameState from storage
     * @throws ConfigInvalidException if the config is invalid
     * @throws ConfigIOException if there is a problem 
     * - creating a new game state file
     * - reading the existing game state file
     * - parsing the game state from json
     */
    public void loadGameState() throws ConfigIOException, ConfigInvalidException {
        this.gameState = gameStateController.getGameState();
        LOGGER.info("Loaded gameState.json");
    }
    
    /**
     * Checks if the game state contains a team with the given name
     * @param teamId The internal name of the team to check for
     * @return True if the team exists in the game state, false otherwise
     */
    public boolean containsTeam(String teamId) {
        return gameState.containsTeam(teamId);
    }
    
    /**
     * Add a team to the game state.
     * @param teamId The internal name of the team.
     * @param teamDisplayName The display name of the team.
     * @param color The color of the team
     * @throws ConfigIOException If there is an error saving the game state while adding a new team.
     */
    public void addTeam(String teamId, String teamDisplayName, String color) throws ConfigIOException {
        gameState.addTeam(teamId, teamDisplayName, color);
        saveGameState();
    }
    
    public void removeTeam(String teamId) throws ConfigIOException {
        gameState.removeTeam(teamId);
        saveGameState();
    }
    
    /**
     * Set up the scoreboard from the game state. 
     * - Add teams to the scoreboard
     * - configure team options
     * - add the players to those teams
     * @param scoreboard The scoreboard to set up
     */
    public void setupScoreboard(Scoreboard scoreboard) {
        unregisterAllTeams(scoreboard);
        registerTeams(scoreboard);
        joinPlayersToTeams(scoreboard);
    }
    
    /**
     * Unregister all teams from the scoreboard
     * @param scoreboard The scoreboard to unregister teams from
     */
    private void unregisterAllTeams(Scoreboard scoreboard) {
        for (Team team : scoreboard.getTeams()) {
            team.unregister();
        }
    }
    
    /**
     * Registers all teams in the game state with the given scoreboard,
     * including the admin team
     * @param scoreboard The scoreboard to register the teams for
     */
    private void registerTeams(Scoreboard scoreboard) {
        Team adminTeam = scoreboard.registerNewTeam(GameManager.ADMIN_TEAM);
        adminTeam.prefix(Component.empty()
                        .append(Component.text("["))
                .append(Component.text("Admin")
                    .color(NamedTextColor.DARK_RED))
                .append(Component.text("]")));
        for (MCTTeam mctTeam : gameState.getTeams().values()) {
            Team team = scoreboard.registerNewTeam(mctTeam.getName());
            team.displayName(Component.text(mctTeam.getDisplayName()));
            NamedTextColor namedTextColor = ColorMap.getNamedTextColor(mctTeam.getColor());
            team.color(namedTextColor);
        }
    }
    
    /**
     * Joins all players in the game state to their respective teams on the given scoreboard
     * @param scoreboard The scoreboard with the teams to join players to
     */
    private void joinPlayersToTeams(Scoreboard scoreboard) {
        for (UUID adminUniqueId : gameState.getAdmins()) {
            Team adminTeam = scoreboard.getTeam(GameManager.ADMIN_TEAM);
            OfflinePlayer admin = Bukkit.getOfflinePlayer(adminUniqueId);
            if (adminTeam == null) {
                // this should never happen
                String message = String.format("Could not find player with UUID %s", adminUniqueId);
                LOGGER.severe(message);
                throw new RuntimeException(message);
            }
            adminTeam.addPlayer(admin);
        }
        for (MCTPlayer mctPlayer : gameState.getPlayers().values()) {
            Team team = scoreboard.getTeam(mctPlayer.getTeamId());
            OfflinePlayer player = Bukkit.getOfflinePlayer(mctPlayer.getUniqueId());
            if (team == null) {
                // this should never happen
                String message = String.format("Could not find team with name %s", mctPlayer.getTeamId());
                LOGGER.severe(message);
                throw new RuntimeException(message);
            }
            team.addPlayer(player);
        }
        
    }
    
    /**
     * Gets a list of the internal names of all the teams in the game state
     * @return A list of all the teams. Empty list if there are no teams.
     */
    public @NotNull Set<String> getTeamIds() {
        return new HashSet<>(gameState.getTeams().keySet());
    }
    
    /**
     * Adds the given player to the game state, joined to the given team
     * @param playerToJoin the UUID of the player
     * @param teamId the teamId to join it to
     * @throws ConfigIOException if there is an IO error saving the game state
     */
    public void addNewPlayer(UUID playerToJoin, String teamId) throws ConfigIOException {
        gameState.addPlayer(playerToJoin, teamId);
        saveGameState();
    }
    
    /**
     * Adds the given offline player to the game state, joined to the given team
     * @param ign the participant's in-game-name
     * @param offlineUniqueId can be null, but represents the offlineUniqueId of the participant
     * @param teamId the teamId of the team this participant belongs to
     * @throws ConfigIOException if there is an IO error saving the game state
     */
    public void addNewOfflineIGN(@NotNull String ign, @Nullable UUID offlineUniqueId, String teamId) {
        gameState.addOfflinePlayer(ign, offlineUniqueId, teamId);
        saveGameState();
    }
    
    /**
     * Checks if the game state contains the given player
     * @param playerUniqueId The UUID of the player to check for
     * @return True if the player with the given UUID exists, false otherwise 
     */
    public boolean containsPlayer(UUID playerUniqueId) {
        return gameState.containsPlayer(playerUniqueId);
    }
    
    /**
     * @param offlineUUID the UUID of the offline player which may be in the GameState
     * @return true if the given UUID matches one of the offline players in the GameState, false otherwise
     */
    public boolean containsOfflinePlayer(UUID offlineUUID) {
        return gameState.getOfflinePlayer(offlineUUID) != null;
    }
    
    /**
     * @param ign the in-game-name of a participant who has never logged in before
     * @return true if the ign is in the current list of offline players (who have yet to log in for the first time), false otherwise
     */
    public boolean containsOfflineIGN(String ign) {
        return gameState.containsOfflineIGN(ign);
    }
    
    /**
     * Gets the internal team name of the player with the given UUID
     * @param playerUniqueId The UUID of the player to find the team of
     * @return The internal team name of the player with the given UUID, null if the game state doesn't contain the player's UUID
     */
    public @Nullable String getPlayerTeamId(@NotNull UUID playerUniqueId) {
        MCTPlayer player = gameState.getPlayer(playerUniqueId);
        if (player != null) {
            return player.getTeamId();
        }
        OfflineMCTPlayer offlineMCTPlayer = gameState.getOfflinePlayer(playerUniqueId);
        if (offlineMCTPlayer != null) {
            return offlineMCTPlayer.getTeamId();
        }
        return null;
    }
    
    /**
     * @param ign the in-game-name of a participant who has never logged in before
     * @return the teamId of the OfflineParticipant with the given IGN. 
     * Null if the IGN doesn't exist in the GameState
     */
    public @Nullable String getOfflineIGNTeamId(@NotNull String ign) {
        OfflineMCTPlayer offlineMCTPlayer = gameState.getOfflinePlayer(ign);
        if (offlineMCTPlayer == null) {
            return null;
        }
        return offlineMCTPlayer.getTeamId();
    }
    
    /**
     * @param ign the in-game-name of a participant who has never logged in before
     * @return the saved IGN of the OfflineParticipant with the given IGN. 
     * Null if the IGN doesn't exist in the GameState
     */
    public @Nullable UUID getOfflineIGNUniqueId(@NotNull String ign) {
        OfflineMCTPlayer offlineMCTPlayer = gameState.getOfflinePlayer(ign);
        if (offlineMCTPlayer == null) {
            return null;
        }
        return offlineMCTPlayer.getOfflineUniqueId();
    }
    
    /**
     * @param uniqueId the UUID to get the offline IGN for
     * @return the IGN of the offlinePlayer with the given UUID. Null if no such player exists. 
     */
    public @Nullable String getOfflineIGN(@NotNull UUID uniqueId) {
        OfflineMCTPlayer offlineMCTPlayer = gameState.getOfflinePlayer(uniqueId);
        if (offlineMCTPlayer == null) {
            return null;
        }
        return offlineMCTPlayer.getIgn();
    }
    
    /**
     * Gets the UUIDs of the players on the given team
     * @param teamId The internal name of the team
     * @return Empty list if no players are on that team, or if the team doesn't exist
     */
    public List<UUID> getParticipantUUIDsOnTeam(String teamId) {
        if (!gameState.containsTeam(teamId)) {
            return Collections.emptyList();
        }
        return gameState.getPlayers().entrySet().stream()
                .filter(mctPlayer -> mctPlayer.getValue().getTeamId().equals(teamId))
                .map(Map.Entry::getKey)
                .toList();
    }
    
    /**
     * Gets the offline in-game-names of the participants on the given team
     * @param teamId the team id
     * @return The in-game-names on the team. Empty list if the team doesn't exist.
     */
    public @NotNull List<String> getOfflineIGNsOnTeam(@NotNull String teamId) {
        if (!gameState.containsTeam(teamId)) {
            return Collections.emptyList();
        }
        return gameState.getOfflinePlayers().values().stream()
                .filter(offlineMCTPlayer -> offlineMCTPlayer.getTeamId().equals(teamId))
                .map(OfflineMCTPlayer::getIgn)
                .toList();
    }
    
    /**
     * Removes the player with the given UUID from the game state, if it exists.
     * If the player did not exist, nothing happens. 
     * @param playerUniqueId The UUID for the player
     * @throws ConfigIOException if there is an IO error saving the game state
     */
    public void leavePlayer(UUID playerUniqueId) throws ConfigIOException {
        gameState.removePlayer(playerUniqueId);
        saveGameState();
    }
    
    /**
     * Removes the offline player with the given IGN from the game state, if it exists.
     * If it did not exist, nothing happens. 
     * @param ign the in-game-name of a player who never logged in
     * @throws ConfigIOException if there is an IO error saving the game state
     */
    public void leaveOfflineIGN(@NotNull String ign) {
        gameState.removeOfflinePlayer(ign);
        saveGameState();
    }
    
    /**
     * @param playerUniqueId the UUID of the player to get the score of. 
     * @return the given participant's score. 0 if the UUID isn't a player, or if it is an offlinePlayer.
     */
    public int getParticipantScore(UUID playerUniqueId) {
        MCTPlayer player = gameState.getPlayer(playerUniqueId);
        if (player == null) {
            return 0;
        }
        return player.getScore();
    }
    
    /**
     * @return a map of each participant's UUID to its score
     */
    public @NotNull Map<UUID, Integer> getParticipantScores() {
        Map<UUID, Integer> participantScores = new HashMap<>(gameState.getPlayers().size());
        for (MCTPlayer mctPlayer : gameState.getPlayers().values()) {
            participantScores.put(mctPlayer.getUniqueId(), mctPlayer.getScore());
        }
        return participantScores;
    }
    
    /**
     * Gets the team color in org.bukkit.Color form for the player with the given UUID
     * @param playerUniqueId The UUID of the player to find the team color for
     * @return The color of the player's team
     */
    public Color getTeamColor(UUID playerUniqueId) {
        String teamId = this.getPlayerTeamId(playerUniqueId);
        String teamColor = gameState.getTeam(teamId).getColor();
        return ColorMap.getColor(teamColor);
    }
    
    public @NotNull NamedTextColor getTeamColor(@NotNull String teamId) {
        String colorString = gameState.getTeam(teamId).getColor();
        return ColorMap.getNamedTextColor(colorString);
    }
    
    public String getTeamDisplayName(String teamId) {
        MCTTeam team = gameState.getTeam(teamId);
        return team.getDisplayName();
    }
    
    /**
     * Not to be confused with {@link #getOfflinePlayerUniqueIds()}
     * @return the UUIDs of the players
     */
    public List<UUID> getPlayerUniqueIds() {
        return gameState.getPlayers().keySet().stream().toList();
    }
    
    /**
     * Not to be confused with {@link #getPlayerUniqueIds()}
     * @return the UUIDs of the offline players 
     */
    public List<UUID> getOfflinePlayerUniqueIds() {
        return gameState.getOfflinePlayers().values().stream().map(OfflineMCTPlayer::getOfflineUniqueId).toList();
    }
    
    /**
     * Add the given score to the given player
     * <b>Important:</b> This does not save the game state, it must be done manually with {@link #saveGameState()}
     * @param uuid the uuid of the player to add the score to
     * @param score the score to add
     */
    public void addScore(UUID uuid, int score) {
        MCTPlayer player = gameState.getPlayers().get(uuid);
        player.setScore(player.getScore() + score);
    }
    
    /**
     * Add the given score to the given players
     * <b>Important:</b> This does not save the game state, it must be done manually with {@link #saveGameState()}
     * @param uuids the uuids of the players to add the score to
     * @param score the score to add
     */
    public void addScorePlayers(Collection<UUID> uuids, int score) {
        for (UUID uuid : uuids) {
            MCTPlayer player = gameState.getPlayers().get(uuid);
            player.setScore(player.getScore() + score);
        }
    }
    
    /**
     * Add the given score to the given team
     * <b>Important:</b> This does not save the game state, it must be done manually with {@link #saveGameState()}
     * @param teamId the teamId of the team to add the score to
     * @param score the score to add
     */
    public void addScore(String teamId, int score) {
        MCTTeam team = gameState.getTeams().get(teamId);
        team.setScore(team.getScore() + score);
    }
    
    /**
     * Adds the given score to each given teamId
     * <b>Important:</b> This does not save the game state, it must be done manually with {@link #saveGameState()}
     * @param teamIds the teamIds to add the score to
     * @param score the score to add
     */
    public void addScoreTeams(Collection<String> teamIds, int score) {
        for (String teamId : teamIds) {
            MCTTeam team = gameState.getTeams().get(teamId);
            team.setScore(team.getScore() + score);
        }
    }
    
    /**
     * Sets the score of the given player to the given value
     * <b>Important:</b> This does not save the game state, it must be done manually with {@link #saveGameState()}
     * @param uuid the uuid of the player to set the score of
     * @param score the score to set to
     */
    public void setScore(UUID uuid, int score) {
        MCTPlayer player = gameState.getPlayers().get(uuid);
        player.setScore(score);
    }
    
    /**
     * Sets the score of the given team to the given value
     * <b>Important:</b> This does not save the game state, it must be done manually with {@link #saveGameState()}
     * @param teamId the uuid of the team to set the score of
     * @param score the score to set to
     */
    public void setScore(String teamId, int score) {
        MCTTeam team = gameState.getTeams().get(teamId);
        team.setScore(score);
    }
    
    /**
     * Sets the score of all teams and players to the given value
     * <b>Important:</b> This does not save the game state, it must be done manually with {@link #saveGameState()}
     * @param score the score to set to
     */
    public void setAllScores(int score) {
        for (MCTPlayer player : gameState.getPlayers().values()) {
            player.setScore(score);
        }
        for (MCTTeam team : gameState.getTeams().values()) {
            team.setScore(score);
        }
    }
    
    public int getTeamScore(String teamId) {
        return gameState.getTeam(teamId).getScore();
    }
    
    /**
     * Gets the color string of the given team
     * @param teamId The teamId to get the color string of
     * @return The color string of the given team
     * @throws NullPointerException if the given teamId is not a valid team
     */
    public String getTeamColorString(@NotNull String teamId) {
        return gameState.getTeam(teamId).getColor();
    }
    
    /**
     * Checks if the given unique id is an admin
     * @param adminUniqueId The admin's unique id to check
     * @return True if the given unique id is an admin, false otherwise
     */
    public boolean isAdmin(UUID adminUniqueId) {
        return gameState.isAdmin(adminUniqueId);
    }
    
    /**
     * Add an admin to the game state
     * @param adminUniqueId the unique id of the admin
     * @throws ConfigIOException If there is an issue saving the game state
     */
    public void addAdmin(UUID adminUniqueId) throws ConfigIOException {
        gameState.addAdmin(adminUniqueId);
        saveGameState();
    }
    
    /**
     * Remove an admin from the game state
     * @param adminUniqueId the unique id of the admin
     * @throws ConfigIOException If there is an issue saving the game state
     */
    public void removeAdmin(UUID adminUniqueId) throws ConfigIOException {
        gameState.removeAdmin(adminUniqueId);
        saveGameState();
    }
}
