package org.braekpo1nt.mctmanager.commands.mctdebug;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class MCTDebugCommandTest {
    
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
        ), MCTDebugCommand.TeamData.getTrimLengths(nameLengths, 35));
        
        Assertions.assertEquals(List.of(
                "Purpled".length(),
                "Antfrost".length(),
                "vGumiho".length(),
                "RedVelvetCak".length()
        ), MCTDebugCommand.TeamData.getTrimLengths(nameLengths, 34));
        
        Assertions.assertEquals(List.of(
                "Purpled".length(),
                "Antfrost".length(),
                "vGumiho".length(),
                "RedVelvetCa".length()
        ), MCTDebugCommand.TeamData.getTrimLengths(nameLengths, 33));
        
        Assertions.assertEquals(List.of(
                "Purpled".length(),
                "Antfros".length(),
                "vGumiho".length(),
                "RedVelve".length()
        ), MCTDebugCommand.TeamData.getTrimLengths(nameLengths, 29));
        
        Assertions.assertEquals(List.of(
                "Purpled".length(),
                "Antfros".length(),
                "vGumiho".length(),
                "RedVelv".length()
        ), MCTDebugCommand.TeamData.getTrimLengths(nameLengths, 28));
        
        Assertions.assertEquals(List.of(
                "Purple".length(),
                "Antfros".length(),
                "vGumiho".length(),
                "RedVelv".length()
        ), MCTDebugCommand.TeamData.getTrimLengths(nameLengths, 27));
        
        Assertions.assertEquals(List.of(
                "Purple".length(),
                "Antfro".length(),
                "vGumiho".length(),
                "RedVelv".length()
        ), MCTDebugCommand.TeamData.getTrimLengths(nameLengths, 26));
        
        Assertions.assertEquals(List.of(
                "Purple".length(),
                "Antfro".length(),
                "vGumih".length(),
                "RedVelv".length()
        ), MCTDebugCommand.TeamData.getTrimLengths(nameLengths, 25));
        
        Assertions.assertEquals(List.of(
                "Purple".length(),
                "Antfro".length(),
                "vGumih".length(),
                "RedVel".length()
        ), MCTDebugCommand.TeamData.getTrimLengths(nameLengths, 24));
        
        Assertions.assertEquals(List.of(
                "Purpl".length(),
                "Antfro".length(),
                "vGumih".length(),
                "RedVel".length()
        ), MCTDebugCommand.TeamData.getTrimLengths(nameLengths, 23));
    }
    
}