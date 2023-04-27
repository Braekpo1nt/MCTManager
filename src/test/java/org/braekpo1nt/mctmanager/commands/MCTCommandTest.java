package org.braekpo1nt.mctmanager.commands;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.UnimplementedOperationException;
import be.seeseemelk.mockbukkit.entity.SimpleEntityMock;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.bukkit.command.PluginCommand;
import org.junit.jupiter.api.*;

import java.util.logging.Level;

public class MCTCommandTest {
    
    private ServerMock server;
    private Main plugin;
    private PluginCommand command;
    
    
    @BeforeEach
    void setUpServerAndPlugin() {
        server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        try {
            plugin = MockBukkit.load(Main.class);
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException while setting up " + this.getClass() + ". MockBukkit must not support the functionality/operation you are trying to test. Check the stack trace below for the exact method that threw the exception. Message from exception:" + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
        command = plugin.getCommand("mct");
    }
    
    @AfterEach
    void tearDownServerAndPlugin() {
        MockBukkit.unmock();
    }
    
    @Test
    @DisplayName("Running /mct with no args returns usage message")
    void noArgsShowsUsage() {
        Assertions.assertTrue(plugin.getMctCommand().onCommand(server.getConsoleSender(), command, "mct", new String[]{}));
        server.getConsoleSender().assertSaid(plugin.getMctCommand().getUsageMessage());
    }
    
}
