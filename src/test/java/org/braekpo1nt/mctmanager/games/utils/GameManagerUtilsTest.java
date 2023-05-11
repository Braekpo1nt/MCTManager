package org.braekpo1nt.mctmanager.games.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GameManagerUtilsTest {
    
    @Test
    @DisplayName("empty returns empty")
    void emptyTest() {
        Map<String, Integer> scores = new HashMap<>();
        String[] firstPlaces = GameManagerUtils.calculateFirstPlace(scores);
        Assertions.assertEquals(0, firstPlaces.length);
    }
    
    @Test
    @DisplayName("one element returns that element")
    void oneTest() {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("a", 0);
        String[] firstPlaces = GameManagerUtils.calculateFirstPlace(scores);
        Assertions.assertEquals(1, firstPlaces.length);
        Assertions.assertEquals("a", firstPlaces[0]);
    }
    
    @Test
    @DisplayName("two equal elements return those elements")
    void twoSameTest() {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("a", 0);
        scores.put("b", 0);
        String[] firstPlaces = GameManagerUtils.calculateFirstPlace(scores);
        Assertions.assertEquals(2, firstPlaces.length);
        Assertions.assertEquals("a", firstPlaces[0]);
        Assertions.assertEquals("b", firstPlaces[1]);
    }
    
    @Test
    @DisplayName("two different scores returns first place")
    void twoDifferentTest() {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("a", 1);
        scores.put("b", 0);
        String[] firstPlaces = GameManagerUtils.calculateFirstPlace(scores);
        Assertions.assertEquals(1, firstPlaces.length);
        Assertions.assertEquals("a", firstPlaces[0]);
    }
    
    @Test
    @DisplayName("three different scores returns first place")
    void threeDifferentTest() {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("a", 2);
        scores.put("b", 1);
        scores.put("c", 0);
        String[] firstPlaces = GameManagerUtils.calculateFirstPlace(scores);
        Assertions.assertEquals(1, firstPlaces.length);
        Assertions.assertEquals("a", firstPlaces[0]);
    }
    
    @Test
    @DisplayName("three equal scores return those scores")
    void threeEqualTest() {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("a", 0);
        scores.put("b", 0);
        scores.put("c", 0);
        String[] firstPlaces = GameManagerUtils.calculateFirstPlace(scores);
        Assertions.assertEquals(3, firstPlaces.length);
        Assertions.assertEquals("a", firstPlaces[0]);
        Assertions.assertEquals("b", firstPlaces[1]);
        Assertions.assertEquals("c", firstPlaces[2]);
    }
    
    @Test
    @DisplayName("two equal high scores and one different return the two equal scores")
    void threeWithTwoHighEqualTest() {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("a", 1);
        scores.put("b", 0);
        scores.put("c", 1);
        String[] firstPlaces = GameManagerUtils.calculateFirstPlace(scores);
        Assertions.assertEquals(2, firstPlaces.length);
        Assertions.assertEquals("a", firstPlaces[0]);
        Assertions.assertEquals("c", firstPlaces[1]);
    }
    
    @Test
    @DisplayName("two equal low scores and one high return the one high")
    void threeWithTwoLowEqualTest() {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("a", 0);
        scores.put("b", 1);
        scores.put("c", 0);
        String[] firstPlaces = GameManagerUtils.calculateFirstPlace(scores);
        Assertions.assertEquals(1, firstPlaces.length);
        Assertions.assertEquals("b", firstPlaces[0]);
    }
    
    @Test
    @DisplayName("diverse scores get first place")
    void diverseTest() {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("a", 500);
        scores.put("b", 600);
        scores.put("c", 154);
        scores.put("d", 300);
        scores.put("e", 300);
        String[] firstPlaces = GameManagerUtils.calculateFirstPlace(scores);
        Assertions.assertEquals(1, firstPlaces.length);
        Assertions.assertEquals("b", firstPlaces[0]);
    }
}