package org.braekpo1nt.mctmanager.commands;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
    
    
}