package org.braekpo1nt.mctmanager.games.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.inventory.InventoryAction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GameManagerUtils {
    
    public static final String TEAM_NAME_REGEX = "[-+\\._A-Za-z0-9]+";
    
    /**
     * A list of all the {@link InventoryAction}s which constitute
     * removing items from the player's inventory
     */
    public final static List<InventoryAction> INV_REMOVE_ACTIONS = List.of(InventoryAction.DROP_ALL_CURSOR, InventoryAction.DROP_ALL_SLOT, InventoryAction.DROP_ONE_CURSOR, InventoryAction.DROP_ONE_SLOT, InventoryAction.MOVE_TO_OTHER_INVENTORY);
    
    /**
     * returns a list that contains the first place, or first place ties.
     * @param teamScores a map pairing teamNames with scores
     * @return An array with the teamName who has the highest score. If there is a tie, returns all tied teamNames. If teamScores is empty, returns empty list.
     */
    public static @NotNull String[] calculateFirstPlace(@NotNull Map<String, Integer> teamScores) {
        if (teamScores.isEmpty()) {
            return new String[0];
        }
        if (teamScores.size() == 1) {
            return teamScores.keySet().toArray(new String[0]);
        }
        
        Iterator<Map.Entry<String, Integer>> iterator = teamScores.entrySet().iterator();
        Map.Entry<String, Integer> initial = iterator.next();
        int firstPlaceScore = initial.getValue();
        List<String> firstPlaces = new ArrayList<>();
        firstPlaces.add(initial.getKey());
        
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> teamScore = iterator.next();
            String teamName = teamScore.getKey();
            int score = teamScore.getValue();
            if (score > firstPlaceScore) {
                firstPlaces.clear();
                firstPlaces.add(teamName);
                firstPlaceScore = score;
            } else if (score == firstPlaceScore) {
                firstPlaces.add(teamName);
            }
        }
        
        return firstPlaces.toArray(new String[0]);
    }
    
    public static boolean validTeamName(String teamName) {
        return teamName.matches(TEAM_NAME_REGEX);
    }
    
    /**
     * @param gameManager the GameManager to get the data from
     * @return a Component with a formatted display of all team and player scores
     */
    public static Component getTeamDisplay(GameManager gameManager) {
        TextComponent.Builder messageBuilder = Component.text().append(Component.text("Scores:\n")
                    .decorate(TextDecoration.BOLD));
        List<OfflinePlayer> offlinePlayers = getSortedOfflinePlayers(gameManager);
        List<String> sortedTeams = getSortedTeams(gameManager);
        
        for (String team : sortedTeams) {
            int teamScore = gameManager.getScore(team);
            NamedTextColor teamNamedTextColor = gameManager.getTeamNamedTextColor(team);
            messageBuilder.append(Component.empty()
                            .append(gameManager.getFormattedTeamDisplayName(team))
                            .append(Component.text(" - "))
                            .append(Component.text(teamScore)
                                    .decorate(TextDecoration.BOLD)
                                    .color(NamedTextColor.GOLD))
                    .append(Component.text(":\n")));
            for (OfflinePlayer offlinePlayer : offlinePlayers) {
                String playerTeam = gameManager.getTeamName(offlinePlayer.getUniqueId());
                int playerScore = gameManager.getScore(offlinePlayer.getUniqueId());
                if (offlinePlayer.getName() == null) {
                    continue;
                }
                if (playerTeam.equals(team)) {
                    messageBuilder.append(Component.empty()
                            .append(Component.text("  "))
                            .append(Component.text(offlinePlayer.getName())
                                    .color(teamNamedTextColor))
                            .append(Component.text(" - "))
                            .append(Component.text(playerScore)
                                    .decorate(TextDecoration.BOLD)
                                    .color(NamedTextColor.GOLD))
                            .append(Component.newline()));
                }
            }
        }
        
        return messageBuilder.build();
    }
    
    /**
     * @param gameManager the GameManager to get the data from
     * @return a sorted list of OfflinePlayers representing the participants. Sorted first by score from greatest to least, then alphabetically (A first, Z last).
     */
    public static @NotNull List<OfflinePlayer> getSortedOfflinePlayers(GameManager gameManager) {
        List<OfflinePlayer> offlinePlayers = gameManager.getOfflineParticipants();
        sortOfflinePlayers(offlinePlayers, gameManager);
        return offlinePlayers;
    }
    
    /**
     * Sorts the provided list of OfflinePlayer objects. Sorts in place.
     * @param offlinePlayers each entry must have a UUID of a valid participant in the GameState of the given GameManager
     * @param gameManager the GameManager to get the data from
     */
    public static void sortOfflinePlayers(List<OfflinePlayer> offlinePlayers, GameManager gameManager) {
        offlinePlayers.sort((p1, p2) -> {
            int scoreComparison = gameManager.getScore(p2.getUniqueId()) - gameManager.getScore(p1.getUniqueId());
            if (scoreComparison != 0) {
                return scoreComparison;
            }
            
            String p1Name = p1.getName();
            if (p1Name == null) {
                p1Name = p1.getUniqueId().toString();
            }
            String p2Name = p2.getName();
            if (p2Name == null) {
                p2Name = p2.getUniqueId().toString();
            }
            return p1Name.compareToIgnoreCase(p2Name);
        });
    }
    
    /**
     * @param gameManager the GameManager to get the data from
     * @return a sorted list of team names. Sorted first by score from greatest to least, then alphabetically (A to Z).
     */
    public static List<String> getSortedTeams(GameManager gameManager) {
        List<String> teamNames = new ArrayList<>(gameManager.getTeamNames());
        teamNames.sort((t1, t2) -> {
            int scoreComparison = gameManager.getScore(t2) - gameManager.getScore(t1);
            if (scoreComparison != 0) {
                return scoreComparison;
            }
            return t1.compareToIgnoreCase(t2);
        });
        return teamNames;
    }
    
    /**
     * This validation and creation is used across multiple commands, so I've put it here for easy replication. 
     * <br>
     * Makes sure the inputs are able to form a valid team that doesn't already exist. 
     * Returns a failure response if anything goes wrong that would prevent the team from being created, otherwise creates the given team. 
     * @param gameManager the GameManager to add the team to
     * @param teamId the teamId
     * @param teamDisplayName the team display name
     * @param colorString the string representing the team's color
     * @return a comprehensive message about the success or failure of the addition of the given team
     */
    public static CommandResult addTeam(GameManager gameManager, @NotNull String teamId, @NotNull String teamDisplayName, @NotNull String colorString) {
        if (gameManager.hasTeam(teamId)) {
            return CommandResult.failure(Component.text("A team already exists with the teamId \"")
                    .append(Component.text(teamId))
                    .append(Component.text("\"")));
        }
        if (teamId.equals(GameManager.ADMIN_TEAM)) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(teamId)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" cannot be "))
                    .append(Component.text(GameManager.ADMIN_TEAM)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" because that is reserved for the admin team.")));
        }
        if (!GameManagerUtils.validTeamName(teamId)) {
            return CommandResult.failure(Component.text("Provide a valid team name\n")
                    .append(Component.text(
                            "Allowed characters: -, +, ., _, A-Z, a-z, and 0-9")));
        }
        
        if (teamDisplayName.isEmpty()) {
            return CommandResult.failure("Display name can't be blank");
        }
        
        if (!ColorMap.hasNamedTextColor(colorString)) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(colorString)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not a recognized color")));
        }
        
        gameManager.addTeam(teamId, teamDisplayName, colorString);
        Component formattedTeamDisplayName = gameManager.getFormattedTeamDisplayName(teamId);
        return CommandResult.success(Component.text("Created team ")
                .append(formattedTeamDisplayName)
                .append(Component.text(" (teamId=\""))
                .append(Component.text(teamId))
                .append(Component.text("\")")));
    }
}
