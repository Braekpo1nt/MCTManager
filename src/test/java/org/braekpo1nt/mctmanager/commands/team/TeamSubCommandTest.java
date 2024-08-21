package org.braekpo1nt.mctmanager.commands.team;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.UnimplementedOperationException;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MockMain;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.MyPlayerMock;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.gamestate.MockGameStateStorageUtil;
import org.braekpo1nt.mctmanager.ui.sidebar.MockSidebarFactory;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.logging.Level;


class TeamSubCommandTest {
    private ServerMock server;
    private Main plugin;
    private PluginCommand command;
    private CommandSender sender;
    private GameManager gameManager;
    
    @BeforeEach
    void setUpServerAndPlugin() {
        server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        try {
            plugin = MockBukkit.load(MockMain.class);
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException while setting up " + this.getClass() + ". MockBukkit must not support the functionality/operation you are trying to test. Check the stack trace below for the exact method that threw the exception. Message from exception:" + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
        gameManager = plugin.getGameManager();
        command = plugin.getCommand("mct");
        sender = server.getConsoleSender();
    }
    
    @AfterEach
    void tearDownServerAndPlugin() {
        MockBukkit.unmock();
    }
    
    @Test
    @DisplayName("`/mct team add ...` adds a new team")
    void addTeamTest() {
        String teamId = "red";
        String teamDisplayName = "Red Team";
        String teamColor = "red";
        plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"team", "add", teamId, String.format("\"%s\"", teamDisplayName), teamColor});
        Assertions.assertTrue(gameManager.hasTeam("red"));
    }
    
    @Test
    @DisplayName("`/mct team join ...` adds a player")
    void addPlayerTest() {
        String teamId = "red";
        String teamDisplayName = "Red Team";
        String teamColor = "red";
        plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"team", "add", teamId, String.format("\"%s\"", teamDisplayName), teamColor});
        Assertions.assertTrue(gameManager.hasTeam("red"));
        String name = "Player1";
        MyPlayerMock player = new MyPlayerMock(server, name, UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)));
        server.addPlayer(player);
        plugin.getMctCommand().onCommand(sender, command, "mct", new String[]{"team", "join", teamId, name});
        Assertions.assertTrue(gameManager.isParticipant(player.getUniqueId()));
    }
    
}