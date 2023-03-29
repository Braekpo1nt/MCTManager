package org.braekpo1nt.mctmanager.games;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.color.ColorMap;
import org.braekpo1nt.mctmanager.games.footrace.FootRaceGame;
import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.braekpo1nt.mctmanager.games.mecha.MechaGame;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

/**
 * Responsible for overall game management. 
 * Creating new game instances, starting/stopping games, and handling game events.
 */
public class GameManager {
    
    private MCTGame activeGame = null;
    private final FootRaceGame footRaceGame;
    private final MechaGame mechaGame;
    private final GameStateStorageUtil gameStateStorageUtil;
    /**
     * Scoreboard for holding the teams. This private scoreboard can't be
     * modified using the normal /team command, and thus can't be unsynced
     * with the game state.
     */
    private final Scoreboard mctScoreboard;
    private final Main plugin;
    private int teleportPlayersToHubTaskId;
    private boolean shouldTeleportToHub = true;
    
    public GameManager(Main plugin, Scoreboard mctScoreboard) {
        this.plugin = plugin;
        this.mctScoreboard = mctScoreboard;
        gameStateStorageUtil = new GameStateStorageUtil(plugin);
        this.footRaceGame = new FootRaceGame(plugin, this);
        this.mechaGame = new MechaGame(plugin, this);
    }
    
    public void loadGameState() throws IOException {
        gameStateStorageUtil.loadGameState();
        gameStateStorageUtil.setupScoreboard(mctScoreboard);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(mctScoreboard);
        }
    }
    
    public void saveGameState() throws IOException, IllegalStateException {
        gameStateStorageUtil.saveGameState();
    }
    
    public void startGame(String gameName, @NotNull CommandSender sender) {
        
        if (activeGame != null) {
            sender.sendMessage("There is already a game running. You must stop the game before you start a new one.");
            return;
        }
        
        List<Player> onlineParticipants = this.getOnlineParticipants();
        if (onlineParticipants.isEmpty()) {
            sender.sendMessage(Component.text("There are no online participants. You can add participants using:\n")
                    .append(Component.text("/mct team join <team> <member>")
                            .decorate(TextDecoration.BOLD)
                            .clickEvent(ClickEvent.suggestCommand("/mct team join "))));
            return;
        }
        
        switch (gameName) {
            case "foot-race":
                footRaceGame.start(onlineParticipants);
                activeGame = footRaceGame;
                break;
            case "mecha":
                mechaGame.start(onlineParticipants);
                activeGame = mechaGame;
                break;
//            case "bedwars":
//                player.sendMessage("3");
//                break;
//            case "capture-the-flag":
//                player.sendMessage("4");
//                break;
//            case "dodgeball":
//                player.sendMessage("5");
//                break;
//            case "spleef":
//                player.sendMessage("6");
//                break;
//            case "parkour-pathway":
//                player.sendMessage("7");
//                break;
            default:
                sender.sendMessage("Unknown game: " + gameName);
                break;
        }
    }
    
    /**
     * gets a list of the online participants
     * @return a list of all online participants
     */
    private List<Player> getOnlineParticipants() {
        List<Player> onlineParticipants = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (gameStateStorageUtil.containsPlayer(player.getUniqueId())) {
                onlineParticipants.add(player);
            }
        }
        return onlineParticipants;
    }
    
    /**
     * If a game is currently going on, manually stops the game. 
     */
    public void manuallyStopGame(CommandSender sender, boolean shouldTeleportToHub) {
        if (activeGame == null) {
            sender.sendMessage("No game is running.");
            return;
        }
        this.shouldTeleportToHub = shouldTeleportToHub;
        activeGame.stop();
        activeGame = null;
    }
    
    /**
     * Meant to be called by the active game when the game is over.
     */
    public void gameIsOver() {
        activeGame = null;
        if (!shouldTeleportToHub) {
            shouldTeleportToHub = true;
            return;
        }
        startDelayedTeleportToHubTask();
    }
    
    private void startDelayedTeleportToHubTask() {
        this.teleportPlayersToHubTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            private int count = 10;
            @Override
            public void run() {
                if (count <= 0) {
                    teleportPlayersToHub();
                    cancelDelayedTeleportToHubTask();
                    return;
                }
                for (Player player : getOnlineParticipants()) {
                    player.sendMessage(Component.text("Teleporting to hub in ")
                            .append(Component.text(count)));
                }
                count--;
            }
        }, 0, 20);
    }
    
    private void cancelDelayedTeleportToHubTask() {
        Bukkit.getScheduler().cancelTask(teleportPlayersToHubTaskId);
    }
    
    private void teleportPlayersToHub() {
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        MultiverseWorld hubWorld = worldManager.getMVWorld("Hub");
        for (Player participant : getOnlineParticipants()) {
            participant.sendMessage("Teleporting to Hub");
            participant.teleport(hubWorld.getSpawnLocation());
        }
    }
    
    /**
     * Remove the given team from the game
     * @param teamName The internal name of the team to remove
     * @return True if the team was successfully removed, false if the team did not exist
     * @throws IOException If there is a problem saving the game state while removing the team
     */
    public boolean removeTeam(String teamName) throws IOException {
        if (!gameStateStorageUtil.containsTeam(teamName)) {
            return false;
        }
        gameStateStorageUtil.removeTeam(teamName);
        Team team = mctScoreboard.getTeam(teamName);
        team.unregister();
        return true;
    }
    
    /**
     * Add a team to the game.
     * @param teamName The internal name of the team.
     * @param teamDisplayName The display name of the team.
     * @return True if the team was successfully created. False if the team already exists.
     * @throws IOException If there was a problem saving the game state with the new team.
     */
    public boolean addTeam(String teamName, String teamDisplayName, String colorString) throws IOException {
        if (gameStateStorageUtil.containsTeam(teamName)) {
            return false;
        }
        gameStateStorageUtil.addTeam(teamName, teamDisplayName, colorString);
        Team newTeam = mctScoreboard.registerNewTeam(teamName);
        newTeam.displayName(Component.text(teamDisplayName));
        NamedTextColor color = ColorMap.getColor(colorString);
        newTeam.color(color);
        return true;
    }
    
    /**
     * A list of all the teams in the game
     * @return A list containing the internal names of all the teams in the game. 
     * Empty list if there are no teams
     */
    public Set<String> getTeamNames() {
        return gameStateStorageUtil.getTeamNames();
    }
    
    /**
     * Checks if the team exists in the game
     * @param teamName The team to look for
     * @return true if the team with the given teamName exists, false otherwise.
     */
    public boolean hasTeam(String teamName) {
        return gameStateStorageUtil.containsTeam(teamName);
    }
    
    /**
     * Joins the player with the given UUID to the team with the given teamName
     * @param playerUniqueId The UUID of the player to join to the given team
     * @param teamName The internal teamName of the team to join the player to. 
     *                 This method assumes the team exists, and will throw a 
     *                 null pointer exception if it doesn't.
     */
    public void joinPlayerToTeam(UUID playerUniqueId, String teamName) throws IOException {
        if (gameStateStorageUtil.containsPlayer(playerUniqueId)) {
            movePlayerToTeam(playerUniqueId, teamName);
            return;
        }
        addNewPlayer(playerUniqueId, teamName);
    }
    
    private void addNewPlayer(UUID playerUniqueId, String teamName) throws IOException {
        gameStateStorageUtil.addNewPlayer(playerUniqueId, teamName);
        Team team = mctScoreboard.getTeam(teamName);
        OfflinePlayer newPlayer = Bukkit.getOfflinePlayer(playerUniqueId);
        team.addPlayer(newPlayer);
    }
    
    private void movePlayerToTeam(UUID playerUniqueId, String newTeamName) {
        String oldTeamName = gameStateStorageUtil.getPlayerTeamName(playerUniqueId);
        gameStateStorageUtil.setPlayerTeamName(playerUniqueId, newTeamName);
    
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUniqueId);
        Team oldTeam = mctScoreboard.getTeam(oldTeamName);
        oldTeam.removePlayer(player);
        Team newTeam = mctScoreboard.getTeam(newTeamName);
        newTeam.addPlayer(player);
    }
    
    public List<String> getPlayerNamesOnTeam(String teamName) {
        List<UUID> playerUniqueIds = gameStateStorageUtil.getPlayerUniqueIdsOnTeam(teamName);
        List<String> playersNamesOnTeam = new ArrayList<>();
        for (UUID playerUniqueId : playerUniqueIds) {
            OfflinePlayer playerOnTeam = Bukkit.getOfflinePlayer(playerUniqueId);
            playersNamesOnTeam.add(playerOnTeam.getName());
        }
        return playersNamesOnTeam;
    }
    
    public boolean hasPlayer(UUID playerUniqueId) {
        return gameStateStorageUtil.containsPlayer(playerUniqueId);
    }
    
    public void leavePlayer(UUID playerUniqueId) throws IOException {
        gameStateStorageUtil.leavePlayer(playerUniqueId);
    }
    
    public String getTeamName(UUID playerUniqueId) {
        return gameStateStorageUtil.getPlayerTeamName(playerUniqueId);
    }
    
    /**
     * Awards points to the player and their team. If the player does not exist, nothing happens.
     * @param player The player to award points to
     * @param points The points to award to the player
     */
    public void awardPointsToPlayer(Player player, int points) {
        UUID playerUniqueId = player.getUniqueId();
        if (!gameStateStorageUtil.containsPlayer(playerUniqueId)) {
            return;
        }
        try {
            gameStateStorageUtil.addPointsToPlayer(playerUniqueId, points);
        } catch (IOException e) {
            player.sendMessage(
                    Component.text("Critical error occurred. Please notify an admin to check the logs.")
                            .color(NamedTextColor.RED)
                            .decorate(TextDecoration.BOLD));
            Bukkit.getLogger().severe("Error while adding points to player. See log for error message.");
            throw new RuntimeException(e);
        }
        player.sendMessage(Component.text("+")
                .append(Component.text(points))
                .append(Component.text(" points"))
                .decorate(TextDecoration.BOLD)
                .color(NamedTextColor.GOLD));
    }
    
    public int getPlayerScore(UUID playerUniqueId) {
        return gameStateStorageUtil.getPlayerScore(playerUniqueId);
    }
}
