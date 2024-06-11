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
     * @param teamName The internal name of the team to check for
     * @return True if the team exists in the game state, false otherwise
     */
    public boolean containsTeam(String teamName) {
        return gameState.containsTeam(teamName);
    }
    
    /**
     * Add a team to the game state.
     * @param teamName The internal name of the team.
     * @param teamDisplayName The display name of the team.
     * @param color The color of the team
     * @throws ConfigIOException If there is an error saving the game state while adding a new team.
     */
    public void addTeam(String teamName, String teamDisplayName, String color) throws ConfigIOException {
        gameState.addTeam(teamName, teamDisplayName, color);
        saveGameState();
    }
    
    public void removeTeam(String teamName) throws ConfigIOException {
        gameState.removeTeam(teamName);
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
            Team team = scoreboard.getTeam(mctPlayer.getTeamName());
            OfflinePlayer player = Bukkit.getOfflinePlayer(mctPlayer.getUniqueId());
            if (team == null) {
                // this should never happen
                String message = String.format("Could not find team with name %s", mctPlayer.getTeamName());
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
    public Set<String> getTeamNames() {
        return new HashSet<>(gameState.getTeams().keySet());
    }
    
    /**
     * Adds the given player to the game state, joined to the given team
     * @param playerToJoin the UUID of the player
     * @param teamName the teamId to join it to
     * @throws ConfigIOException if there is an IO error saving the game state
     */
    public void addNewPlayer(UUID playerToJoin, String teamName) throws ConfigIOException {
        gameState.addPlayer(playerToJoin, teamName);
        saveGameState();
    }
    
    /**
     * Adds the given offline player to the game state, joined to the given team
     * @param ign the participant's in-game-name
     * @param offlineUniqueId can be null, but represents the offlineUniqueId of the participant
     * @param teamName the teamId of the team this participant belongs to
     * @throws ConfigIOException if there is an IO error saving the game state
     */
    public void addNewOfflineIGN(@NotNull String ign, @Nullable UUID offlineUniqueId, String teamName) {
        gameState.addOfflinePlayer(ign, offlineUniqueId, teamName);
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
     * @param ign the in-game-name of a participant who has never logged in before
     * @return true if the ign is in the current list of offline players (who have yet to log in for the first time), false otherwise
     */
    public boolean containsOfflineIGN(String ign) {
        return gameState.containsOfflineIGN(ign);
    }
    
    /**
     * Gets the internal team name of the player with the given UUID
     * @param playerUniqueId The UUID of the player to find the team of
     * @return The internal team name of the player with the given UUID
     * @throws NullPointerException if the game state doesn't contain the player's UUID
     */
    public String getPlayerTeamName(UUID playerUniqueId) {
        return gameState.getPlayer(playerUniqueId).getTeamName();
    }
    
    /**
     * @param ign the in-game-name of a participant who has never logged in before
     * @return the teamId of the OfflineParticipant with the given ign
     * @throws NullPointerException if the ign doesn't exist in the GameState
     */
    public @NotNull String getOfflineIGNTeamName(@NotNull String ign) {
        return gameState.getOfflinePlayer(ign).getTeamName();
    }
    
    /**
     * Gets the UUIDs of the players on the given team
     * @param teamName The internal name of the team
     * @return Empty list if no players are on that team, or if the team doesn't exist
     */
    public List<UUID> getPlayerUniqueIdsOnTeam(String teamName) {
        if (!gameState.containsTeam(teamName)) {
            return Collections.emptyList();
        }
        return gameState.getPlayers().entrySet().stream()
                .filter(mctPlayer -> mctPlayer.getValue().getTeamName().equals(teamName))
                .map(Map.Entry::getKey)
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
    
    public int getPlayerScore(UUID playerUniqueId) {
        return gameState.getPlayer(playerUniqueId).getScore();
    }
    
    /**
     * Gets the team color in org.bukkit.Color form for the player with the given UUID
     * @param playerUniqueId The UUID of the player to find the team color for
     * @return The color of the player's team
     */
    public Color getTeamColor(UUID playerUniqueId) {
        String teamName = this.getPlayerTeamName(playerUniqueId);
        String teamColor = gameState.getTeam(teamName).getColor();
        return ColorMap.getColor(teamColor);
    }
    
    public NamedTextColor getTeamNamedTextColor(String teamName) {
        String colorString = gameState.getTeam(teamName).getColor();
        return ColorMap.getNamedTextColor(colorString);
    }
    
    public String getTeamDisplayName(String teamName) {
        MCTTeam team = gameState.getTeam(teamName);
        return team.getDisplayName();
    }
    
    public List<UUID> getPlayerUniqueIds() {
        return gameState.getPlayers().keySet().stream().toList();
    }
    
    public void addScore(UUID uniqueId, int score) throws ConfigIOException {
        MCTPlayer player = gameState.getPlayers().get(uniqueId);
        player.setScore(player.getScore() + score);
        saveGameState();
    }
    
    public void addScore(String teamName, int score) throws ConfigIOException {
        MCTTeam team = gameState.getTeams().get(teamName);
        team.setScore(team.getScore() + score);
        saveGameState();
    }
    
    public void setScore(UUID uniqueId, int score) throws ConfigIOException {
        MCTPlayer player = gameState.getPlayers().get(uniqueId);
        player.setScore(score);
        saveGameState();
    }

    public void setScore(String teamName, int score) throws ConfigIOException {
        MCTTeam team = gameState.getTeams().get(teamName);
        team.setScore(score);
        saveGameState();
    }
    
    public void setAllScores(int score) throws ConfigIOException {
        for (MCTPlayer player : gameState.getPlayers().values()) {
            player.setScore(score);
        }
        for (MCTTeam team : gameState.getTeams().values()) {
            team.setScore(score);
        }
        saveGameState();
    }
    
    public int getTeamScore(String teamName) {
        return gameState.getTeam(teamName).getScore();
    }
    
    /**
     * Gets the color string of the given team
     * @param teamName The team to get the color string of
     * @return The color string of the given team
     */
    public String getTeamColorString(String teamName) {
        return gameState.getTeam(teamName).getColor();
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
