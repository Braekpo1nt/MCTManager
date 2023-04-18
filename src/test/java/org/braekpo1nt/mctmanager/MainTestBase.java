package org.braekpo1nt.mctmanager;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.UnimplementedOperationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;


public class MainTestBase {
    
    protected ServerMock server;
    protected Main plugin;
    
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
    
    
}
