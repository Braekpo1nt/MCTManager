package org.braekpo1nt.mctmanager.hub;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.jetbrains.annotations.NotNull;

public class MockHubManager extends HubManager {
    public MockHubManager(Main plugin, GameManager gameManager) {
        super(plugin, gameManager);
    }
    
    @Override
    public void loadConfig(@NotNull String configFile) throws ConfigIOException, ConfigInvalidException {
        this.config = configController.getDefaultConfig();
        // intentionally skipping addition of leaderboardManagers
    }
}
