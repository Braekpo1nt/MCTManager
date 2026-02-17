package org.braekpo1nt.mctmanager.games.gamestate;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.database.entities.admin.AdminEntity;
import org.braekpo1nt.mctmanager.database.entities.participants.ActiveParticipant;
import org.braekpo1nt.mctmanager.database.entities.teams.ActiveTeam;
import org.braekpo1nt.mctmanager.database.service.GameStateService;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.participant.ColorAttributes;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Handles the CRUD operations for storing GameState objects
 * keeps the most recently loaded GameState in memory so that each call to it doesn't need to be a file IO operation
 */
public class GameStateStorageUtil {
    
    private final Logger LOGGER;
    private final GameStateService gameStateService;
    protected GameState gameState = new GameState(new HashMap<>(), new HashMap<>(), new ArrayList<>());
    
    public GameStateStorageUtil(@NotNull Main plugin, @NotNull GameStateService gameStateService) {
        this.LOGGER = plugin.getLogger();
        // Pro Tip: The plugin.getGameManager() is null at this point
        this.gameStateService = gameStateService;
    }
    
    /**
     * Load the GameState from storage
     * @throws ConfigInvalidException if the config is invalid
     * @throws ConfigIOException if there is a problem
     * - creating a new game state file
     * - reading the existing game state file
     * - parsing the game state from json
     */
    public void loadGameState() throws ConfigIOException, ConfigInvalidException, SQLException {
        this.gameState = constructGameStateFromDatabase();
        LOGGER.info("Loaded gameState.json");
    }
    
    private @NotNull GameState constructGameStateFromDatabase() throws SQLException {
        List<ActiveTeam> activeTeams = gameStateService.getActiveTeams();
        List<ActiveParticipant> activeParticipants = gameStateService.getActiveParticipants();
        List<AdminEntity> adminEntities = gameStateService.getAdmins();
        return new GameState(toPlayers(activeParticipants), toTeams(activeTeams), toAdmins(adminEntities));
    }
    
    public static Map<String, MCTTeamEntity> toTeams(List<ActiveTeam> activeTeams) {
        return activeTeams.stream()
                .map(GameStateStorageUtil::toTeam)
                .collect(Collectors.toMap(MCTTeamEntity::getName, Function.identity()));
    }
    
    public static List<UUID> toAdmins(List<AdminEntity> adminEntities) {
        return adminEntities.stream()
                .map(admin -> UUID.fromString(admin.getUuid()))
                .collect(Collectors.toCollection(ArrayList::new));
    }
    
    public static MCTTeamEntity toTeam(ActiveTeam activeTeam) {
        return MCTTeamEntity.builder()
                .name(activeTeam.getTeamId())
                .displayName(activeTeam.getDisplayName())
                .score(activeTeam.getScore())
                .color(activeTeam.getColor())
                .build();
    }
    
    public static Map<UUID, MCTPlayerEntity> toPlayers(List<ActiveParticipant> activeParticipants) {
        return activeParticipants.stream()
                .map(GameStateStorageUtil::toPlayer)
                .collect(Collectors.toMap(MCTPlayerEntity::getUniqueId, Function.identity()));
    }
    
    public static MCTPlayerEntity toPlayer(ActiveParticipant activeParticipant) {
        return MCTPlayerEntity.builder()
                .uniqueId(UUID.fromString(activeParticipant.getParticipantUUID()))
                .name(activeParticipant.getIgn())
                .score(activeParticipant.getScore())
                .teamId(activeParticipant.getTeamId())
                .build();
    }
    
    public static List<ActiveTeam> fromTeams(Collection<MCTTeamEntity> entities) {
        return entities.stream()
                .map(GameStateStorageUtil::fromTeam)
                .toList();
    }
    
    public static ActiveTeam fromTeam(MCTTeamEntity team) {
        return ActiveTeam.builder()
                .teamId(team.getName())
                .displayName(team.getDisplayName())
                .color(team.getColor())
                .score(team.getScore())
                .build();
    }
    
    public List<ActiveParticipant> fromPlayers(Collection<MCTPlayerEntity> entities) {
        return entities.stream()
                .map(GameStateStorageUtil::fromPlayer)
                .toList();
    }
    
    private static ActiveParticipant fromPlayer(MCTPlayerEntity player) {
        return ActiveParticipant.builder()
                .participantUUID(player.getUniqueId().toString())
                .teamId(player.getTeamId())
                .ign(player.getName())
                .score(player.getScore())
                .build();
    }
    
    /**
     * Add a team to the game state.
     * @param teamId The internal name of the team.
     * @param teamDisplayName The display name of the team.
     * @param color The color of the team
     * @throws ConfigIOException If there is an error saving the game state while adding a new team.
     */
    public void addTeam(String teamId, String teamDisplayName, String color) throws ConfigIOException, SQLException {
        MCTTeamEntity team = gameState.addTeam(teamId, teamDisplayName, color);
        gameStateService.addTeam(fromTeam(team));
    }
    
    public void removeTeam(String teamId) throws ConfigIOException, SQLException {
        List<UUID> uuidsOnTeam = this.getParticipantUUIDsOnTeam(teamId);
        gameState.removePlayers(uuidsOnTeam);
        gameState.removeTeam(teamId);
        gameStateService.deleteTeam(teamId);
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
        for (MCTTeamEntity mctTeam : gameState.getTeams().values()) {
            Team team = scoreboard.registerNewTeam(mctTeam.getName());
            team.displayName(Component.text(mctTeam.getDisplayName()));
            NamedTextColor namedTextColor = ColorMap.getNamedTextColor(mctTeam.getColor());
            team.color(namedTextColor);
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
    public void addNewPlayer(@NotNull UUID playerToJoin, @NotNull String name, @NotNull String teamId) throws ConfigIOException, SQLException {
        MCTPlayerEntity player = gameState.addPlayer(playerToJoin, name, teamId);
        gameStateService.addParticipant(fromPlayer(player));
    }
    
    /**
     * @param uuid the UUID of the participant to get
     * @return the OfflineParticipant from the given UUID, or null if the UUID isn't in the game state
     */
    public @Nullable OfflineParticipant getOfflineParticipant(@NotNull UUID uuid) {
        MCTPlayerEntity player = gameState.getPlayer(uuid);
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
    
    public void updateScores(Collection<org.braekpo1nt.mctmanager.games.gamemanager.MCTTeam> teams, Collection<OfflineParticipant> participants) throws Exception {
        List<ActiveTeam> activeTeams = new ArrayList<>(teams.size());
        for (org.braekpo1nt.mctmanager.games.gamemanager.MCTTeam team : teams) {
            MCTTeamEntity mctTeam = gameState.getTeam(team.getTeamId());
            mctTeam.setScore(team.getScore());
            activeTeams.add(fromTeam(mctTeam));
        }
        
        List<ActiveParticipant> activeParticipants = new ArrayList<>(participants.size());
        for (OfflineParticipant participant : participants) {
            MCTPlayerEntity player = Objects.requireNonNull(
                    gameState.getPlayer(participant.getUniqueId()),
                    "attempted to update the score of a participant who is not in the GameState");
            player.setScore(participant.getScore());
            activeParticipants.add(fromPlayer(player));
        }
        
        persistScores(activeParticipants, activeTeams);
    }
    
    /**
     * @param activeParticipants the participants to commit to the database
     * @param activeTeams the teams to commit to the database
     * @throws Exception if there is an issue communicating with the database
     */
    protected void persistScores(List<ActiveParticipant> activeParticipants, List<ActiveTeam> activeTeams) throws Exception {
        gameStateService.updateActiveParticipants(activeParticipants);
        gameStateService.updateActiveTeams(activeTeams);
    }
    
    public void updateScore(OfflineParticipant participant) throws SQLException {
        MCTPlayerEntity player = Objects.requireNonNull(gameState.getPlayer(participant.getUniqueId()),
                "attempted to update score of non-existent participant");
        player.setScore(participant.getScore());
        gameStateService.updateActiveParticipant(fromPlayer(player));
    }
    
    public void updateScore(org.braekpo1nt.mctmanager.games.gamemanager.MCTTeam mctTeam) throws SQLException {
        MCTTeamEntity team = gameState.getTeam(mctTeam.getTeamId());
        team.setScore(mctTeam.getScore());
        gameStateService.updateActiveTeam(fromTeam(team));
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
    public void leavePlayer(UUID playerUniqueId) throws ConfigIOException, SQLException {
        gameState.removePlayer(playerUniqueId);
        gameStateService.deleteParticipant(playerUniqueId.toString());
    }
    
    public @NotNull NamedTextColor getTeamColor(@NotNull String teamId) {
        String colorString = gameState.getTeam(teamId).getColor();
        return ColorMap.getNamedTextColor(colorString);
    }
    
    public @NotNull ColorAttributes getTeamColorAttributes(@NotNull String teamId) {
        String colorString = gameState.getTeam(teamId).getColor();
        return ColorMap.getColorAttributes(colorString);
    }
    
    public String getTeamDisplayName(String teamId) {
        MCTTeamEntity team = gameState.getTeam(teamId);
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
    public void addAdmin(UUID adminUniqueId) throws ConfigIOException, SQLException {
        gameState.addAdmin(adminUniqueId);
        gameStateService.addAdmin(new AdminEntity(adminUniqueId.toString()));
    }
    
    /**
     * Remove an admin from the game state
     * @param adminUniqueId the unique id of the admin
     * @throws ConfigIOException If there is an issue saving the game state
     */
    public void removeAdmin(UUID adminUniqueId) throws ConfigIOException, SQLException {
        gameState.removeAdmin(adminUniqueId);
        gameStateService.deleteAdmin(adminUniqueId.toString());
    }
    
}
