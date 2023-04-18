package org.braekpo1nt.mctmanager;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.UnimplementedOperationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MainTest {
    
    private ServerMock server;
    private Main plugin;
    
    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock(new MyCustomServerMock());
        try {
            plugin = MockBukkit.load(Main.class);
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException while setting up MainTest. MockBukkit must not support the functionality/operation you are trying to test. Check the stack trace below for the exact method that threw the exception. Message from exception:" + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }
    
    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }
    
    @Test
    public void checkSumHere() {
        int three = 3;
        int four = 4;
        int sum = three + four;
        Assertions.assertEquals(7, sum);
    }
    
}
