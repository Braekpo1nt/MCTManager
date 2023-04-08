package org.braekpo1nt.mctmanager.games.gamestate;

import com.google.gson.Gson;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.color.ColorMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.*;
import java.util.*;

/**
 * Handles the CRUD operations for storing GameState objects
 */
public class GameStateStorageUtil {
    
    private static final String GAME_STATE_FILE_NAME = "gameState.json";
    private final File gameStateDirectory;
    private GameState gameState = new GameState();
    
    public GameStateStorageUtil(Main plugin) {
        this.gameStateDirectory = plugin.getDataFolder().getAbsoluteFile();
    }
    
    /**
     * Save the GameState to storage
     * @throws IOException if there is a problem
     * - creating a new game state file
     * - writing to the game state file
     * - converting the game state to json
     */
    public void saveGameState() throws IOException {
        Gson gson = new Gson();
        File gameStateFile = getGameStateFile();
        Writer writer = new FileWriter(gameStateFile, false);
        gson.toJson(this.gameState, writer);
        writer.flush();
        writer.close();
        Bukkit.getLogger().info("[MCTManager] Saved game state.");
    }
    
    /**
     * Load the GameState from storage
     * @throws IOException if there is a problem 
     * - creating a new game state file
     * - reading the existing game state file
     * - parsing the game state from json
     */
    public void loadGameState() throws IOException {
        Gson gson = new Gson();
        File gameStateFile = getGameStateFile();
        Reader reader = new FileReader(gameStateFile);
        GameState newGameState = gson.fromJson(reader, GameState.class);
        reader.close();
        if (newGameState == null) {
            newGameState = new GameState();
        }
        this.gameState = newGameState;
        Bukkit.getLogger().info("[MCTManager] Loaded game state.");
    }
    
    /**
     * Get the game state file from the given game state directory. Creates the file
     * if one doesn't already exist.
     * @return The game state file
     * @throws IOException if there is an error creating a new game state file
     */
    private File getGameStateFile() throws IOException {
        File gameStateFile = new File(gameStateDirectory.getAbsolutePath(), this.GAME_STATE_FILE_NAME);
        if (!gameStateFile.exists()) {
            if (!gameStateDirectory.exists()) {
                gameStateDirectory.mkdirs();
            }
            gameStateFile.createNewFile();
        }
        return gameStateFile;
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
     * @throws IOException If there is an error saving the game state while adding a new team.
     */
    public void addTeam(String teamName, String teamDisplayName, String color) throws IOException {
        gameState.addTeam(teamName, teamDisplayName, color);
        saveGameState();
    }
    
    public void removeTeam(String teamName) throws IOException {
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
     * Registers all teams in the game state with the given scoreboard
     * @param scoreboard The scoreboard to register the teams for
     */
    private void registerTeams(Scoreboard scoreboard) {
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
        for (MCTPlayer mctPlayer : gameState.getPlayers().values()) {
            Team team = scoreboard.getTeam(mctPlayer.getTeamName());
            OfflinePlayer player = Bukkit.getOfflinePlayer(mctPlayer.getUniqueId());
            team.addPlayer(player);
        }
        
    }
    
    /**
     * Gets a list of the internal names of all the teams in the game state
     * @return A list of all the teams. Empty list if there are no teams.
     */
    public Set<String> getTeamNames() {
        return gameState.getTeams().keySet();
    }
    
    
    public void addNewPlayer(UUID playerToJoin, String teamName) throws IOException {
        gameState.addPlayer(playerToJoin, teamName);
        saveGameState();
    }
    
    public boolean containsPlayer(UUID playerUniqueId) {
        return gameState.containsPlayer(playerUniqueId);
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
     * Sets the team name of the player with the given UUID
     * @param playerUniqueId The UUID of the player to set the team name of
     * @param teamName The team name
     */
    public void setPlayerTeamName(UUID playerUniqueId, String teamName) {
        gameState.getPlayer(playerUniqueId).setTeamName(teamName);
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
    
    public void leavePlayer(UUID playerUniqueId) throws IOException {
        gameState.removePlayer(playerUniqueId);
        saveGameState();
    }
    
    public void addPointsToPlayer(UUID playerUniqueId, int points) throws IOException {
        MCTPlayer mctPlayer = gameState.getPlayer(playerUniqueId);
        mctPlayer.setScore(mctPlayer.getScore() + points);
        MCTTeam mctTeam = gameState.getTeam(mctPlayer.getTeamName());
        mctTeam.setScore(mctTeam.getScore() + points);
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
    
    public ChatColor getTeamChatColor(String teamName) {
        String teamColor = gameState.getTeam(teamName).getColor();
        return ColorMap.getChatColor(teamColor);
    }
    
    public List<UUID> getPlayerUniqueIds() {
        return gameState.getPlayers().keySet().stream().toList();
    }
    
    public void addScore(UUID uniqueId, int score) throws IOException {
        MCTPlayer player = gameState.getPlayers().get(uniqueId);
        player.setScore(player.getScore() + score);
        saveGameState();
    }
    
    public void addScore(String teamName, int score) throws IOException {
        MCTTeam team = gameState.getTeams().get(teamName);
        team.setScore(team.getScore() + score);
        saveGameState();
    }

    public void setScore(UUID uniqueId, int score) throws IOException {
        MCTPlayer player = gameState.getPlayers().get(uniqueId);
        player.setScore(score);
        saveGameState();
    }

    public void setScore(String teamName, int score) throws IOException {
        MCTTeam team = gameState.getTeams().get(teamName);
        team.setScore(score);
        saveGameState();
    }

    public int getTeamScore(String teamName) {
        return gameState.getTeam(teamName).getScore();
    }
    
    public String getTeamColorString(String teamName) {
        return gameState.getTeam(teamName).getColor();
    }
}
