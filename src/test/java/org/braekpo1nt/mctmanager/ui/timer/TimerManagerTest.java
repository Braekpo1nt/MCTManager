package org.braekpo1nt.mctmanager.ui.timer;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.UnimplementedOperationException;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MockMain;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.TestUtils;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.footrace.config.FootRaceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;

class TimerManagerTest {
    
    private TimerManager timerManager;
    private GameManager gameManager;
    private Main plugin;
    private ServerMock server;
    
    @BeforeEach
    void setup() {
        server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        try {
            plugin = MockBukkit.load(MockMain.class);
            gameManager = plugin.getGameManager();
            timerManager = gameManager.getTimerManager();
            InputStream inputStream = FootRaceConfig.class.getResourceAsStream("exampleFootRaceConfig.json");
            TestUtils.copyInputStreamToFile(inputStream, new File(plugin.getDataFolder(), "footRaceConfig.json"));
        } catch (UnimplementedOperationException ex) {
            System.out.println("UnimplementedOperationException while setting up " + this.getClass() + ". MockBukkit must not support the functionality/operation you are trying to test. Check the stack trace below for the exact method that threw the exception. Message from exception:" + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }
    
    @Test
    void test() {
        TimerManager subManager = timerManager.createManager();
        subManager.start(Timer.builder()
                        .duration(1)
                        .build());
        timerManager.skip();
    }
    
    @Test
    void footRace() {
        PlayerMock playerMock = server.addPlayer();
        gameManager.addTeam("test", "Test", "white");
        gameManager.joinPlayerToTeam(plugin.getServer().getConsoleSender(), playerMock, "test");
        gameManager.startGame(GameType.FOOT_RACE, plugin.getServer().getConsoleSender());
        timerManager.skip();
    }
    
}