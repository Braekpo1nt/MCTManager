package org.braekpo1nt.mctmanager.games.game.mecha.config;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.MyCustomServerMock;
import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtilTest;
import org.junit.jupiter.api.BeforeEach;

import java.util.logging.Level;

public class MechaStorageUtilTest extends GameConfigStorageUtilTest<MechaConfig> {

    protected MechaStorageUtilTest() {
        super("validMechaConfig.json", "invalidMechaConfig.json", "mechaConfig.json");
    }

    @BeforeEach
    void setupServerAndPlugin() {
        ServerMock server = MockBukkit.mock(new MyCustomServerMock());
        server.getLogger().setLevel(Level.OFF);
        plugin = MockBukkit.load(Main.class);
        storageUtil = new MechaStorageUtil(plugin.getDataFolder());
    }
}
