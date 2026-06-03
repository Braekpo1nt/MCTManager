package org.braekpo1nt.mctmanager.games.gamestate;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.database.entities.admin.ActiveAdminEntity;
import org.braekpo1nt.mctmanager.database.entities.participants.ActiveParticipant;
import org.braekpo1nt.mctmanager.database.entities.teams.ActiveTeam;
import org.braekpo1nt.mctmanager.database.service.GameStateService;
import org.braekpo1nt.mctmanager.database.service.RegisterConflictType;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamestate.states.SUEventState;
import org.braekpo1nt.mctmanager.games.gamestate.states.SUMaintenanceState;
import org.braekpo1nt.mctmanager.games.gamestate.states.SUPracticeState;
import org.braekpo1nt.mctmanager.games.gamestate.states.StorageUtilState;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the CRUD operations for storing GameState objects
 * keeps the most recently loaded GameState in memory so that each call to it doesn't need to be a file IO operation
 */
public class GameStateStorageUtil {
    
    protected final Logger LOGGER;
    @Getter
    protected final GameStateService gameStateService;
    @Getter
    protected final Executor databaseExecutor;
    /**
     * A flag that is only needed because the tests use sqlite, and production uses mariaDB.
     * Specifically regarding the portability miss-match in conflict resolution during upsert operations.
     */
    @Getter
    protected final boolean mariaDB;
    @Getter
    protected GameState gameState = new GameState(new HashMap<>(), new HashMap<>(), new ArrayList<>());
    protected @NotNull StorageUtilState state;
    
    public GameStateStorageUtil(@NotNull Logger logger, @NotNull GameStateService gameStateService, @NotNull Executor databaseExecutor) {
        this(logger, gameStateService, databaseExecutor, true);
    }
    
    public GameStateStorageUtil(@NotNull Logger logger, @NotNull GameStateService gameStateService, @NotNull Executor databaseExecutor, boolean mariaDB) {
        this.LOGGER = logger;
        // Pro Tip: The plugin.getGameManager() is null at this point
        this.gameStateService = gameStateService;
        this.databaseExecutor = databaseExecutor;
        this.mariaDB = mariaDB;
        this.state = new SUMaintenanceState(this);
        state.enter();
    }
    
    public void setState(@NotNull StorageUtilState state) {
        this.state.exit();
        this.state = state;
        this.state.enter();
    }
    
    public void maintenanceMode() {
        setState(new SUMaintenanceState(this));
    }
    
    public void practiceMode() {
        setState(new SUPracticeState(this));
    }
    
    public void eventMode(@NotNull String eventId) {
        setState(new SUEventState(this, eventId));
    }
    
    /**
     * Load the GameState from storage
     * @throws ConfigInvalidException if the config is invalid
     * @throws ConfigIOException if there is a problem
     * - creating a new game state file
     * - reading the existing game state file
     * - parsing the game state from json
     */
    public CompletableFuture<Void> loadGameState() throws SQLException {
        return constructGameStateFromDatabase()
                .thenAccept(gameState -> {
                    // yes, this successfully assigns the gameState to this.gameState even though it's threaded operation.
                    // You are guaranteed to have this.gameState updated provided you wait till the completion of this
                    // future, e.g. {@code .thenApply} or {@code .thenRunAsync} etc. with proper chaining
                    // You only need to label this.gameState as volatile if unrelated threads (improperly chained) access it
                    this.gameState = gameState;
                    LOGGER.info("Constructed game state from database");
                })
                .thenAcceptAsync(gameState -> {
                    try {
                        gameStateService.setActiveVersion(0);
                    } catch (SQLException e) {
                        Main.logger().log(Level.WARNING, "database exception while trying to set the active version", e);
                    }
                }, databaseExecutor)
                .exceptionally(e -> {
                    Main.logger().log(Level.SEVERE, "Unable to load gameState from database", e);
                    return null;
                });
    }
    
    private @NotNull CompletableFuture<GameState> constructGameStateFromDatabase() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<ActiveTeam> activeTeams = gameStateService.getActiveTeams();
                List<ActiveParticipant> activeParticipants = gameStateService.getActiveParticipants();
                List<ActiveAdminEntity> adminEntities = gameStateService.getActiveAdmins();
                return new GameState(ActiveParticipant.toPlayers(activeParticipants), ActiveTeam.toTeams(activeTeams), ActiveAdminEntity.toAdmins(adminEntities));
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }, databaseExecutor);
    }
    
    public Component printGameState() {
        TextComponent.Builder builder = Component.text();
        builder.append(Component.text("Teams: "))
                .append(Component.newline());
        for (MCTTeamEntity team : gameState.getTeams().values()) {
            builder
                    .append(Component.text("--"))
                    .append(Component.text(team.getName()))
                    .append(Component.text(", "))
                    .append(Component.text(team.getScore()))
                    .append(Component.newline())
            ;
            
        }
        builder.append(Component.text("Participants: "))
                .append(Component.newline());
        for (MCTPlayerEntity player : gameState.getPlayers().values()) {
            builder
                    .append(Component.text("-----"))
                    .append(Component.text(player.getName()))
                    .append(Component.text(", "))
                    .append(Component.text(player.getTeamId()))
                    .append(Component.text(", "))
                    .append(Component.text(player.getScore()))
                    .append(Component.newline())
            ;
        }
        
        builder.append(Component.text("Admins: "))
                .append(Component.newline());
        for (UUID admin : gameState.getAdmins()) {
            builder
                    .append(Component.text("--"))
                    .append(Component.text(admin.toString()))
                    .append(Component.newline())
            ;
        }
        return builder.build();
    }
    
    /**
     * Add a team to the game state.
     * @param teamId The internal name of the team.
     * @param teamDisplayName The display name of the team.
     * @param color The color of the team
     * @return the score of the team (based on historical data)
     * @throws ConfigIOException If there is an error saving the game state while adding a new team.
     */
    public CompletableFuture<Integer> addTeam(String teamId, String teamDisplayName, String color) {
        return state.addTeam(teamId, teamDisplayName, color);
    }
    
    public CompletableFuture<Void> removeTeam(String teamId) {
        return state.removeTeam(teamId);
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
    
    public RegisterConflictType registerPlayer(@NotNull UUID uuid, @NotNull String ign) throws SQLException {
        RegisterConflictType type = gameStateService.registerPlayer(uuid.toString(), ign);
        
        // TODO: handle admins
        return switch (type) {
            case MIGRATE_IGN -> {
                // a player with the UUID exists but the IGN is wrong
                MCTPlayerEntity participantWithWrongIGN = gameState.getPlayer(uuid);
                if (participantWithWrongIGN == null) {
                    // they're not in the game state, no changes needed
                    yield RegisterConflictType.NONE;
                }
                // the player is a participant in the game state, so we need to migrate the ign
                participantWithWrongIGN.setName(ign);
                yield RegisterConflictType.MIGRATE_IGN;
            }
            case MIGRATE_UUID -> {
                // a player with the ign exists, but the UUID is wrong
                MCTPlayerEntity participantWithIgn = gameState.getPlayer(ign);
                if (participantWithIgn == null) {
                    // they're not in the game state, no changes needed
                    yield RegisterConflictType.NONE;
                }
                migrateFromUUIDToUUID(
                        participantWithIgn.getUniqueId(),
                        participantWithIgn.getTeamId(),
                        participantWithIgn.getScore(),
                        uuid,
                        ign
                );
                yield RegisterConflictType.MIGRATE_UUID;
            }
            default -> RegisterConflictType.NONE;
        };
    }
    
    private boolean resolveConflicts(@NotNull UUID uuid, @NotNull String ign) {
        MCTPlayerEntity existingPlayer = gameState.getPlayer(uuid);
        if (existingPlayer != null) {
            // check if the ign is right
            if (existingPlayer.getName().equals(ign)) {
                // everything is correct, we are done
                return false;
            }
            // if the UUID exists but the ign is wrong, we need to change the ign
            MCTPlayerEntity playerWithIgn = gameState.getPlayer(ign);
            if (playerWithIgn == null) {
                // there are no existing participants with the ign, so just update the ign
                existingPlayer.setName(ign);
                return true;
            }
            // there is a participant with the ign, so we need to migrate the UUID and keep the ign
            migrateFromUUIDToUUID(playerWithIgn.getUniqueId(), playerWithIgn.getTeamId(), playerWithIgn.getScore(), uuid, ign);
            return true;
        }
        // a participant with the UUID does not exist in the participants list
        MCTPlayerEntity playerWithIgn = gameState.getPlayer(ign);
        if (playerWithIgn == null) {
            // no players with the ign exist
            return false;
        }
        // a participant with the wrong UUID but the right IGN exists in the participants list
        // we must migrate the UUID and keep the correct IGN
        migrateFromUUIDToUUID(playerWithIgn.getUniqueId(), playerWithIgn.getTeamId(), playerWithIgn.getScore(), uuid, ign);
        return true;
    }
    
    private void migrateFromUUIDToUUID(UUID fromUUID, String teamId, int score, UUID toUUID, String ign) {
        gameState.removePlayer(fromUUID);
        MCTPlayerEntity correctedPlayer = gameState.addPlayer(toUUID, ign, teamId);
        correctedPlayer.setScore(score);
    }
    
    /**
     * Adds the given player to the game state, joined to the given team
     * @param uuid the UUID of the player
     * @param ign the ign of the player
     * @param teamId the teamId to join it to
     * @throws ConfigIOException if there is an IO error saving the game state
     */
    public CompletableFuture<Integer> joinPlayer(@NotNull UUID uuid, @NotNull String ign, @NotNull String teamId) {
        return state.joinPlayer(uuid, ign, teamId);
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
    
    /**
     * Update the scores in memory. Does not persist to the database unless you call
     * {@link #persistScores(Collection, Collection)}.
     * @param teams the teams to persist the scores for
     * @param participants the participants to persist the scores for
     */
    public void updateScores(Collection<org.braekpo1nt.mctmanager.games.gamemanager.MCTTeam> teams, Collection<OfflineParticipant> participants) {
        for (org.braekpo1nt.mctmanager.games.gamemanager.MCTTeam team : teams) {
            MCTTeamEntity mctTeam = gameState.getTeam(team.getTeamId());
            mctTeam.setScore(team.getScore());
        }
        
        for (OfflineParticipant participant : participants) {
            MCTPlayerEntity player = Objects.requireNonNull(
                    gameState.getPlayer(participant.getUniqueId()),
                    String.format("attempted to update the score of a participant who is not in the GameState \"%s\"", participant.getUniqueId()));
            player.setScore(participant.getScore());
        }
    }
    
    /**
     * Used to persist the current score values of the given teams and participants to the
     * database. Meant to be called after {@link #updateScores(Collection, Collection)}
     * @param teams the teams to update
     * @param participants the participants to update
     */
    @SuppressWarnings("UnusedReturnValue")
    public CompletableFuture<Void> persistScores(Collection<org.braekpo1nt.mctmanager.games.gamemanager.MCTTeam> teams, Collection<OfflineParticipant> participants) {
        List<ActiveTeam> activeTeams = teams.stream()
                .map(team -> {
                    MCTTeamEntity mctTeamEntity = gameState.getTeam(team.getTeamId());
                    return ActiveTeam.fromTeam(mctTeamEntity);
                })
                .filter(Objects::nonNull)
                .toList();
        List<ActiveParticipant> activeParticipants = participants.stream()
                .map(participant -> {
                    MCTPlayerEntity mctPlayerEntity = gameState.getPlayer(participant.getUniqueId());
                    return ActiveParticipant.fromPlayer(mctPlayerEntity);
                })
                .filter(Objects::nonNull)
                .toList();
        return persistScores(activeParticipants, activeTeams);
    }
    
    /**
     * @param activeParticipants the participants to commit to the database
     * @param activeTeams the teams to commit to the database
     */
    protected CompletableFuture<Void> persistScores(List<ActiveParticipant> activeParticipants, List<ActiveTeam> activeTeams) {
        return CompletableFuture.runAsync(() -> {
            try {
                gameStateService.updateActiveParticipants(activeParticipants);
                gameStateService.updateActiveTeams(activeTeams);
            } catch (Exception e) {
                Main.logger().log(Level.SEVERE, String.format("Unable to persist scores of %s participants and %s teams", activeParticipants.size(), activeTeams.size()), e);
            }
        }, databaseExecutor);
    }
    
    /**
     * Update the score of the given participant in-memory.
     * Asynchronously persist the score of the given participant in the database.
     * @param participant the participant to update the score of
     */
    @SuppressWarnings("UnusedReturnValue")
    public CompletableFuture<Void> persistScore(OfflineParticipant participant) {
        MCTPlayerEntity player = Objects.requireNonNull(gameState.getPlayer(participant.getUniqueId()),
                "attempted to persist score of non-existent participant");
        player.setScore(participant.getScore());
        return CompletableFuture.runAsync(() -> {
            try {
                gameStateService.updateActiveParticipant(ActiveParticipant.fromPlayer(player));
            } catch (Exception e) {
                Main.logger().log(Level.SEVERE, String.format("Unable to persist scores of participant %s score=%s", player.getName(), player.getScore()), e);
            }
        }, databaseExecutor);
    }
    
    /**
     * Update the score of the given team in-memory.
     * Asynchronously persist the score of the given team in the database.
     * @param mctTeam the team to update the score of
     */
    @SuppressWarnings("UnusedReturnValue")
    public CompletableFuture<Void> persistScore(org.braekpo1nt.mctmanager.games.gamemanager.MCTTeam mctTeam) {
        MCTTeamEntity team = Objects.requireNonNull(gameState.getTeam(mctTeam.getTeamId()),
                "attempted to persist score of non-existent team");
        team.setScore(mctTeam.getScore());
        return CompletableFuture.runAsync(() -> {
            try {
                gameStateService.updateActiveTeam(ActiveTeam.fromTeam(team));
            } catch (Exception e) {
                Main.logger().log(Level.SEVERE, String.format("Unable to persist scores of team %s score=%s", team.getName(), team.getScore()), e);
            }
        }, databaseExecutor);
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
    public CompletableFuture<Void> leavePlayer(UUID playerUniqueId) {
        return state.leavePlayer(playerUniqueId);
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
     * @return a list of the UUIDs of the admins
     */
    public List<UUID> getAdminUUIDs() {
        return gameState.getAdmins();
    }
    
    /**
     * @return a map from UUID to Admin Names
     */
    public @NotNull CompletableFuture<Map<UUID, String>> getAllAdminNames() {
        return state.getAllAdminNames();
    }
    
    /**
     * Add an admin to the game state
     * @param adminUniqueId the unique id of the admin
     * @throws ConfigIOException If there is an issue saving the game state
     */
    public void addAdmin(UUID adminUniqueId) throws SQLException {
        state.addAdmin(adminUniqueId);
    }
    
    /**
     * Remove an admin from the game state
     * @param adminUniqueId the unique id of the admin
     * @throws ConfigIOException If there is an issue saving the game state
     */
    public CompletableFuture<Void> removeAdmin(UUID adminUniqueId) {
        return state.removeAdmin(adminUniqueId);
    }
    
    public void setIGN(UUID uuid, String ign) throws SQLException {
        MCTPlayerEntity player = gameState.getPlayer(uuid);
        if (player == null) {
            return;
        }
        player.setName(ign);
        gameStateService.migrateIgn(uuid.toString(), ign);
    }
}
