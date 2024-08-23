package org.braekpo1nt.mctmanager.commands.mctdebug;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class MCTDebugCommandTest {
    
    @Test
    void calculateMaxNameLength() {
        int actual = MCTDebugCommand.Team.calculateMaxNameLength(List.of(12, 12, 12, 12), 48);
        Assertions.assertEquals(11, actual);
    }
    
    @Test
    void getParticipantsLine() {
        List<Integer> nameLengths = List.of(
                "Purpled".length(),
                "Antfrost".length(),
                "vGumiho".length(),
                "RedVelvetCake".length()
        );
        List<Integer> thirtyEight = getTrimLengths(nameLengths, 38);
        Assertions.assertEquals(List.of(7, 8, 7, 13), thirtyEight);
        
        List<Integer> thirtySeven = getTrimLengths(nameLengths, 37);
        Assertions.assertEquals(List.of(7, 8, 7, 12), thirtySeven);
        
        List<Integer> thirtyOne = getTrimLengths(nameLengths, 31);
        Assertions.assertEquals(List.of(7, 8, 7, 7), thirtyOne);
    }
    
    
    static List<Integer> getTrimLengths(List<Integer> nameLengths, int maxLineLength) {
        // Initialize the trimmed lengths as a copy of the original lengths
        List<Integer> trimmedLengths = new ArrayList<>(nameLengths);
        
        // Calculate the initial total length with spaces between names
        int totalLength = nameLengths.stream().mapToInt(Integer::intValue).sum() + (nameLengths.size() - 1);
        
        if (totalLength <= maxLineLength) {
            // If total length is already within the limit, return the original lengths
            return trimmedLengths;
        }
        
        
        // Sort the indices of lengths by the corresponding values in descending order
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < nameLengths.size(); i++) {
            indices.add(i);
        }
        indices.sort((a, b) -> nameLengths.get(b) - nameLengths.get(a));
        
        // Iteratively reduce the length of the longest names
        while (totalLength > maxLineLength) {
            for (int index : indices) {
                if (trimmedLengths.get(index) > 0) {
                    trimmedLengths.set(index, trimmedLengths.get(index) - 1);
                    totalLength--;
                    
                    if (totalLength <= maxLineLength) {
                        break;
                    }
                }
            }
        }
        
        return trimmedLengths;
    }
    
}