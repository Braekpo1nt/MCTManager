package org.braekpo1nt.mctmanager.games.utils;

import java.util.*;

public class GameManagerUtils {
    /**
     * returns a list that contains the first and second place teams.
     * @param teamScores a map pairing teamNames with scores
     * @return If there is a clear first and second place, returns an array with the first element as the first place, and the second element as the second place. If there are ties, returns all tied teams. If teamScores is empty, returns empty. If teamScores size is 1, returns the only team.
     */
    public static String[] calculateFirstAndSecondPlace(Map<String, Integer> teamScores) {
        if (teamScores.isEmpty()) {
            return new String[]{};
        }
        if (teamScores.size() == 1) {
            return teamScores.keySet().toArray(new String[1]);
        }
        if (teamScores.size() == 2) {
            return teamScores.keySet().toArray(new String[2]);
        }
        Iterator<Map.Entry<String, Integer>> iterator = teamScores.entrySet().iterator();
        
        List<String> highestScores = new ArrayList<>();
        Map.Entry<String, Integer> initialHighest = iterator.next();
        int highest = initialHighest.getValue();
        highestScores.add(initialHighest.getKey());
        
        List<String> secondHighestScores = new ArrayList<>();
        Map.Entry<String, Integer> initialSecondHighest = iterator.next();
        int secondHighest = initialSecondHighest.getValue();
        secondHighestScores.add(initialSecondHighest.getKey());
        
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            String teamName = entry.getKey();
            int score = entry.getValue();
            
            if (score > highest) {
                highestScores.clear();
                highestScores.add(teamName);
                highest = score;
            } else if (score == highest) {
                highestScores.add(teamName);
            } else if (score > secondHighest) {
                secondHighestScores.clear();
                secondHighestScores.add(teamName);
                secondHighest = score;
            } else if (score == secondHighest) {
                secondHighestScores.add(teamName);
            }
        }
        
        List<String> results = new ArrayList<>();
        results.addAll(highestScores);
        results.addAll(secondHighestScores);
        return results.toArray(new String[2]);
    }
}
