package org.braekpo1nt.mctmanager.games.utils;

import java.util.*;

public class GameManagerUtils {
    public static String[] calculateFirstAndSecondPlace(Map<String, Integer> scores) {
        if (scores.isEmpty()) {
            return new String[]{};
        }
        if (scores.size() == 2) {
            return scores.keySet().toArray(new String[2]);
        }
        Iterator<Map.Entry<String, Integer>> iterator = scores.entrySet().iterator();
        
        Map<String, Integer> highestScores = new HashMap<>();
        Map.Entry<String, Integer> initialHighest = iterator.next();
        highestScores.put(initialHighest.getKey(), initialHighest.getValue());
        
        Map<String, Integer> secondHighestScores = new HashMap<>();
        Map.Entry<String, Integer> initialSecondHighest = iterator.next();
        secondHighestScores.put(initialSecondHighest.getKey(), initialSecondHighest.getValue());
        
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            String teamName = entry.getKey();
            int score = entry.getValue();
            
            
        }
        
        List<String> results = new ArrayList<>();
        results.addAll(highestScores.keySet());
        results.addAll(secondHighestScores.keySet());
        return results.toArray(new String[2]);
    }
}
