package org.braekpo1nt.mctmanager.games.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GameManagerUtilsTest {
    
    @Test
    @DisplayName("two equal scores return in alphabetical order")
    void firstAndSecondTest() {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("b", 0);
        scores.put("a", 0);
        String[] firstAndSecondPlace = GameManagerUtils.calculateFirstAndSecondPlace(scores);
        Assertions.assertEquals("a", firstAndSecondPlace[0]);
        Assertions.assertEquals("b", firstAndSecondPlace[1]);
    }
    
    @Test
    @DisplayName("two different scores return highest in first, lowest in second")
    void twoTest() {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("a", 1);
        scores.put("b", 0);
        String[] firstAndSecondPlace = GameManagerUtils.calculateFirstAndSecondPlace(scores);
        Assertions.assertEquals("a", firstAndSecondPlace[0]);
        Assertions.assertEquals("b", firstAndSecondPlace[1]);
    }
    
    @Test
    @DisplayName("three different scores return first and second")
    void threeTest() {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("a", 2);
        scores.put("b", 1);
        scores.put("c", 0);
        String[] firstAndSecondPlace = GameManagerUtils.calculateFirstAndSecondPlace(scores);
        Assertions.assertEquals(2, firstAndSecondPlace.length);
        Assertions.assertEquals("a", firstAndSecondPlace[0]);
        Assertions.assertEquals("b", firstAndSecondPlace[1]);
    }
    
    @Test
    @DisplayName("three scores with two highest equal return two highest in alphabetical order")
    void threeHighEqualTest() {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("b", 1);
        scores.put("a", 1);
        scores.put("c", 0);
        String[] firstAndSecondPlace = GameManagerUtils.calculateFirstAndSecondPlace(scores);
        Assertions.assertEquals(2, firstAndSecondPlace.length);
        Assertions.assertEquals("a", firstAndSecondPlace[0]);
        Assertions.assertEquals("b", firstAndSecondPlace[1]);
    }
    
    @Test
    @DisplayName("three equal scores return 3 values")
    void threeEqualTest() {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("b", 0);
        scores.put("a", 0);
        scores.put("c", 0);
        String[] firstAndSecondPlace = GameManagerUtils.calculateFirstAndSecondPlace(scores);
        Assertions.assertEquals(3, firstAndSecondPlace.length);
    }
    
    @Test
    @DisplayName("three with two lowest equal return three values")
    void threeLowEqualTest() {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("a", 1);
        scores.put("b", 0);
        scores.put("c", 0);
        String[] firstAndSecondPlace = GameManagerUtils.calculateFirstAndSecondPlace(scores);
        Assertions.assertEquals(3, firstAndSecondPlace.length);
    }
    
    @Test
    @DisplayName("four scores return first and second")
    void fourTest() {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("a", 3);
        scores.put("b", 2);
        scores.put("c", 1);
        scores.put("d", 0);
        String[] firstAndSecondPlace = GameManagerUtils.calculateFirstAndSecondPlace(scores);
        Assertions.assertEquals(2, firstAndSecondPlace.length);
        Assertions.assertEquals("a", firstAndSecondPlace[0]);
        Assertions.assertEquals("b", firstAndSecondPlace[1]);
    }
    
    @Test
    @DisplayName("four scores with the two lowest equal return first and second")
    void fourLowestEqualTest() {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("a", 2);
        scores.put("b", 1);
        scores.put("c", 0);
        scores.put("d", 0);
        String[] firstAndSecondPlace = GameManagerUtils.calculateFirstAndSecondPlace(scores);
        Assertions.assertEquals(2, firstAndSecondPlace.length);
        Assertions.assertEquals("a", firstAndSecondPlace[0]);
        Assertions.assertEquals("b", firstAndSecondPlace[1]);
    }
    
    @Test
    @DisplayName("four scores with the two highest equal return first and second")
    void fourHighestEqualTest() {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("a", 2);
        scores.put("b", 2);
        scores.put("c", 1);
        scores.put("d", 0);
        String[] firstAndSecondPlace = GameManagerUtils.calculateFirstAndSecondPlace(scores);
        Assertions.assertEquals(2, firstAndSecondPlace.length);
        Assertions.assertEquals("a", firstAndSecondPlace[0]);
        Assertions.assertEquals("b", firstAndSecondPlace[1]);
    }
    
    @Test
    @DisplayName("empty returns empty")
    void emptyTest() {
        Map<String, Integer> scores = new HashMap<>();
        String[] firstAndSecondPlace = GameManagerUtils.calculateFirstAndSecondPlace(scores);
        Assertions.assertEquals(0, firstAndSecondPlace.length);
    }
    
    @Test
    @DisplayName("one element returns that element")
    void singleTest() {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("a", 0);
        String[] firstAndSecondPlace = GameManagerUtils.calculateFirstAndSecondPlace(scores);
        Assertions.assertEquals(1, firstAndSecondPlace.length);
        Assertions.assertEquals("a", firstAndSecondPlace[0]);
    }
    
}