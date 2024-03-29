package org.braekpo1nt.mctmanager.games.utils;

import org.bukkit.event.inventory.InventoryAction;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GameManagerUtils {
    
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
}
