package org.braekpo1nt.mctmanager.commands;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class CommandUtilsTest {
    
    @Test
    void removeElementTest1() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> CommandUtils.removeElement(new String[]{"1"}, 1));
    }
    
    @Test
    void removeElementTest2() {
        @NotNull String[] newArgs = CommandUtils.removeElement(new String[]{"1", "2"}, 1);
        Assertions.assertArrayEquals(newArgs, new String[]{"1"});
    }
    
    @Test
    void removeElementTest4() {
        @NotNull String[] newArgs = CommandUtils.removeElement(new String[]{"1", "2", "3", "4"}, 1);
        Assertions.assertArrayEquals(newArgs, new String[]{"1", "3", "4"});
    }
    
    @Test
    void test() {
        List<String> teamIds = List.of(
                "purple",
                "red",
                "yellow"
        );
        Assertions.assertEquals(List.of("red purple yellow"), CommandUtils.suggestGreedyList("red purple yellow", teamIds));
        Assertions.assertEquals(List.of(), CommandUtils.suggestGreedyList("red purple pur", teamIds));
        Assertions.assertEquals(List.of("red purple yellow"), CommandUtils.suggestGreedyList("red purple ", teamIds));
        Assertions.assertEquals(List.of("red purple"), CommandUtils.suggestGreedyList("red purple", teamIds));
        Assertions.assertEquals(List.of("purple", "red", "yellow"), CommandUtils.suggestGreedyList("", teamIds));
        Assertions.assertEquals(List.of("red purple yellow"), CommandUtils.suggestGreedyList("red purple yello", teamIds));
        Assertions.assertEquals(List.of("red yellow purple"), CommandUtils.suggestGreedyList("red yellow pur", teamIds));
        Assertions.assertEquals(List.of("red purple yellow"), CommandUtils.suggestGreedyList("red purple yello", teamIds));
        Assertions.assertEquals(List.of("red purple", "red yellow"), CommandUtils.suggestGreedyList("red ", teamIds));
        Assertions.assertEquals(List.of("red"), CommandUtils.suggestGreedyList("red", teamIds));
        
        Assertions.assertEquals(List.of(), CommandUtils.suggestGreedyList("red o", teamIds));
        Assertions.assertEquals(List.of(), CommandUtils.suggestGreedyList("o", teamIds));
        Assertions.assertEquals(List.of("o red"), CommandUtils.suggestGreedyList("o r", teamIds));
    }
    
    
}
