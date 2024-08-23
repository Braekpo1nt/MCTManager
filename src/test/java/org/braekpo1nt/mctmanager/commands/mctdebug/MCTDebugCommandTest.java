package org.braekpo1nt.mctmanager.commands.mctdebug;

import org.jetbrains.annotations.NotNull;
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
    void getTrimLengthsTest() {
        List<Integer> nameLengths = List.of(
                "Purpled".length(),
                "Antfrost".length(),
                "vGumiho".length(),
                "RedVelvetCake".length()
        );
        
        Assertions.assertEquals(List.of(
                "Purpled".length(),
                "Antfrost".length(),
                "vGumiho".length(),
                "RedVelvetCake".length()
        ), getTrimLengths(nameLengths, 35));
        
        Assertions.assertEquals(List.of(
                "Purpled".length(),
                "Antfrost".length(),
                "vGumiho".length(),
                "RedVelvetCak".length()
        ), getTrimLengths(nameLengths, 34));
        
        Assertions.assertEquals(List.of(
                "Purpled".length(),
                "Antfrost".length(),
                "vGumiho".length(),
                "RedVelvetCa".length()
        ), getTrimLengths(nameLengths, 33));
        
        Assertions.assertEquals(List.of(
                "Purpled".length(),
                "Antfros".length(),
                "vGumiho".length(),
                "RedVelve".length()
        ), getTrimLengths(nameLengths, 29));
        
        Assertions.assertEquals(List.of(
                "Purpled".length(),
                "Antfros".length(),
                "vGumiho".length(),
                "RedVelv".length()
        ), getTrimLengths(nameLengths, 28));
        
        Assertions.assertEquals(List.of(
                "Purple".length(),
                "Antfros".length(),
                "vGumiho".length(),
                "RedVelv".length()
        ), getTrimLengths(nameLengths, 27));
        
        Assertions.assertEquals(List.of(
                "Purple".length(),
                "Antfro".length(),
                "vGumiho".length(),
                "RedVelv".length()
        ), getTrimLengths(nameLengths, 26));
        
        Assertions.assertEquals(List.of(
                "Purple".length(),
                "Antfro".length(),
                "vGumih".length(),
                "RedVelv".length()
        ), getTrimLengths(nameLengths, 25));
        
        Assertions.assertEquals(List.of(
                "Purple".length(),
                "Antfro".length(),
                "vGumih".length(),
                "RedVel".length()
        ), getTrimLengths(nameLengths, 24));
        
        Assertions.assertEquals(List.of(
                "Purpl".length(),
                "Antfro".length(),
                "vGumih".length(),
                "RedVel".length()
        ), getTrimLengths(nameLengths, 23));
    }
    
    @Test
    void getTrimLengthsTest2() {
        List<Integer> nameLengths = List.of(
                7,
                8,
                7,
                13
        );
        
        Assertions.assertEquals(List.of(
                7,
                8,
                7,
                13
        ), getTrimLengths(nameLengths, 35));
        
        Assertions.assertEquals(List.of(
                7,
                8,
                7,
                12
        ), getTrimLengths(nameLengths, 34));
        
        Assertions.assertEquals(List.of(
                7,
                8,
                7,
                11
        ), getTrimLengths(nameLengths, 33));
        
        Assertions.assertEquals(List.of(
                7,
                7,
                7,
                8
        ), getTrimLengths(nameLengths, 29));
        
        Assertions.assertEquals(List.of(
                7,
                7,
                7,
                7
        ), getTrimLengths(nameLengths, 28));
        
        Assertions.assertEquals(List.of(
                6,
                7,
                7,
                7
        ), getTrimLengths(nameLengths, 27));
        
        Assertions.assertEquals(List.of(
                6,
                6,
                7,
                7
        ), getTrimLengths(nameLengths, 26));
        
        Assertions.assertEquals(List.of(
                6,
                6,
                6,
                7
        ), getTrimLengths(nameLengths, 25));
        
        Assertions.assertEquals(List.of(
                6,
                6,
                6,
                6
        ), getTrimLengths(nameLengths, 24));
        
        Assertions.assertEquals(List.of(
                5,
                6,
                6,
                6
        ), getTrimLengths(nameLengths, 23));
    }
    
    public static @NotNull List<Integer> getTrimLengths(@NotNull List<Integer> nameLengths, int maxLineLength) {
        List<Integer> trimmedLengths = new ArrayList<>(nameLengths);
        if (trimmedLengths.isEmpty()) {
            return trimmedLengths;
        }
        int totalLength = trimmedLengths.stream().mapToInt(Integer::intValue).sum();
        int numOfNames = trimmedLengths.size();
        while (totalLength > maxLineLength) {
            int maxIndex = 0;
            int maxValue = trimmedLengths.getFirst();
            for (int i = 1; i < numOfNames; i++) {
                int value = trimmedLengths.get(i);
                if (value > maxValue) {
                    maxValue = value;
                    maxIndex = i;
                }
            }
            trimmedLengths.set(maxIndex, maxValue - 1);
            totalLength--;
        }
        return trimmedLengths;
    }
    
}