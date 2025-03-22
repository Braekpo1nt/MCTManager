package org.braekpo1nt.mctmanager.games.event;

import lombok.Data;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Data
public class Tip {
    private final int priority;
    private final Component body;

    /**
     * Selects tips with weighted randomness based on priority
     *
     * @param tips List of tips to select from
     * @return A randomly selected tip, with higher priority tips more likely to be
     *         chosen
     */
    public static Tip selectWeightedRandomTip(List<Tip> tips) {
        if (tips == null || tips.isEmpty()) {
            throw new IllegalArgumentException("Tips list cannot be null or empty");
        }

        int totalWeight = tips.stream()
                .mapToInt(Tip::getPriority)
                .sum();

        Random random = new Random();
        int randomPoint = random.nextInt(totalWeight);

        // Select tip based on weighted randomness
        int cumulativeWeight = 0;
        for (Tip tip : tips) {
            cumulativeWeight += tip.getPriority();
            if (randomPoint < cumulativeWeight) {
                return tip;
            }
        }

        return tips.getFirst();
    }

    /**
     * Selects multiple tips with weighted randomness, ensuring no repeats
     * 
     * @param tips  list of tips to select from
     * @param count number of tips to select
     * @return list of unique tips
     */
    public static List<Tip> selectMultipleWeightedRandomTips(List<Tip> tips, int count) {
        List<Tip> selectedTips = new ArrayList<>();
        List<Tip> remainingTips = new ArrayList<>(tips);

        while (selectedTips.size() < count) {
            // If remaining tips is empty, refill with original tips
            if (remainingTips.isEmpty()) {
                remainingTips = new ArrayList<>(tips);
            }

            Tip selectedTip = selectWeightedRandomTip(remainingTips);

            if (!selectedTips.contains(selectedTip)) {
                selectedTips.add(selectedTip);
            }

            remainingTips.remove(selectedTip);
        }
        return selectedTips;
    }
}
