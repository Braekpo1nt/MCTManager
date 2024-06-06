package org.braekpo1nt.mctmanager.games.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.games.GameManager;
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
        TextComponent.Builder messageBuilder = Component.text().append(Component.text("TEAMS\n")
                    .decorate(TextDecoration.BOLD));
        List<OfflinePlayer> offlinePlayers = gameManager.getOfflineParticipants();
        List<String> teamNames = gameManager.getTeamNames().stream().toList();
        
        for (String teamName : teamNames) {
            int teamScore = gameManager.getScore(teamName);
            NamedTextColor teamNamedTextColor = gameManager.getTeamNamedTextColor(teamName);
            messageBuilder.append(Component.empty()
                            .append(gameManager.getFormattedTeamDisplayName(teamName))
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
                if (playerTeam.equals(teamName)) {
                    messageBuilder.append(Component.empty()
                            .append(Component.text("  "))
                            .append(Component.text(offlinePlayer.getName())
                                    .color(teamNamedTextColor))
                            .append(Component.text(" - "))
                            .append(Component.text(playerScore)
                                    .decorate(TextDecoration.BOLD)
                                    .color(NamedTextColor.GOLD))
                            .append(Component.text("\n")));
                }
            }
        }
        
        return messageBuilder.build();
    }
    
    /**
     * @return a sorted list of OfflinePlayers representing the participants. Sorted first by score from greatest to least, then alphabetically (A first, Z last).
     */
    public static @NotNull List<OfflinePlayer> getSortedOfflinePlayers(GameManager gameManager) {
        List<OfflinePlayer> offlinePlayers = gameManager.getOfflineParticipants();
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
        return offlinePlayers;
    }
    
    /**
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
}
