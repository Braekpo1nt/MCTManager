package org.braekpo1nt.mctmanager.games.gamestate;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
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
    protected GameState gameState = new GameState(new HashMap<>(), new HashMap<>(), new ArrayList<>());
    
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
    public void setupScoreboard(Scoreboard scoreboard, Server server) {
        unregisterAllTeams(scoreboard);
        registerTeams(scoreboard);
        joinPlayersToTeams(scoreboard, server);
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
    private void joinPlayersToTeams(Scoreboard scoreboard, Server server) {
        for (UUID adminUniqueId : gameState.getAdmins()) {
            Team adminTeam = scoreboard.getTeam(GameManager.ADMIN_TEAM);
            OfflinePlayer admin = server.getOfflinePlayer(adminUniqueId);
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
            OfflinePlayer player = server.getOfflinePlayer(mctPlayer.getUniqueId());
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
     * @param name the name of the player
     * @param teamId the teamId to join it to
     * @throws ConfigIOException if there is an IO error saving the game state
     */
    public void addNewPlayer(@NotNull UUID playerToJoin, @NotNull String name, @NotNull String teamId) throws ConfigIOException {
        gameState.addPlayer(playerToJoin, name, teamId);
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
     * @param uuid the UUID of the participant to get
     * @return the OfflineParticipant from the given UUID, or null if the UUID isn't in the game state
     */
    public @Nullable OfflineParticipant getOfflineParticipant(@NotNull UUID uuid) {
        MCTPlayer player = gameState.getPlayer(uuid);
        if (player == null) {
            return null;
        }
        NamedTextColor teamColor = getTeamColor(player.getTeamId());
        return new OfflineParticipant(
                player.getUniqueId(),
                player.getName(),
                GameManagerUtils.createDisplayName(player.getName(), teamColor),
                player.getTeamId(),
                player.getScore()
        );
    }
    
    public void updateScores(Collection<org.braekpo1nt.mctmanager.participant.MCTTeam> teams, Collection<OfflineParticipant> participants) {
        updateTeamScores(teams);
        updateParticipantScores(participants);
    }
    
    public void updateScore(OfflineParticipant participant) {
        Objects.requireNonNull(gameState.getPlayer(participant.getUniqueId()),
                        "attempted to update score of non-existent participant")
                .setScore(participant.getScore());
    }
    
    public void updateParticipantScores(Collection<OfflineParticipant> participants) {
        for (OfflineParticipant participant : participants) {
            MCTPlayer player = Objects.requireNonNull(
                    gameState.getPlayer(participant.getUniqueId()),
                    "attempted to update the score of a participant who is not in the GameState");
            player.setScore(participant.getScore());
        }
    }
    
    public void updateScore(org.braekpo1nt.mctmanager.participant.MCTTeam team) {
        gameState.getTeam(team.getTeamId()).setScore(team.getScore());
    }
    
    public void updateTeamScores(Collection<org.braekpo1nt.mctmanager.participant.MCTTeam> teams) {
        for (org.braekpo1nt.mctmanager.participant.MCTTeam team : teams) {
            MCTTeam mctTeam = gameState.getTeam(team.getTeamId());
            mctTeam.setScore(team.getScore());
        }
    }
    
    /**
     * Gets the internal team name of the player with the given UUID
     * @param uuid The UUID of the player to find the team of
     * @return The internal team name of the player with the given UUID, null if the game state doesn't contain the player's UUID
     */
    public @Nullable String getPlayerTeamId(@NotNull UUID uuid) {
        MCTPlayer player = gameState.getPlayer(uuid);
        if (player != null) {
            return player.getTeamId();
        }
        return null;
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
    
    public @NotNull NamedTextColor getTeamColor(@NotNull String teamId) {
        String colorString = gameState.getTeam(teamId).getColor();
        return ColorMap.getNamedTextColor(colorString);
    }
    
    public String getTeamDisplayName(String teamId) {
        MCTTeam team = gameState.getTeam(teamId);
        return team.getDisplayName();
    }
    
    /**
     * @return the UUIDs of the players
     */
    public List<UUID> getPlayerUniqueIds() {
        return gameState.getPlayers().keySet().stream().toList();
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
