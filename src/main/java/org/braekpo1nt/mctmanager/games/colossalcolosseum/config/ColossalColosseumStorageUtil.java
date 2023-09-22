package org.braekpo1nt.mctmanager.games.colossalcolosseum.config;

import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;

public class ColossalColosseumStorageUtil extends GameConfigStorageUtil<ColossalColosseumConfig> {
    /**
     * @param configDirectory The directory that the config should be located in (e.g. the plugin's data folder)
     */
    public ColossalColosseumStorageUtil(File configDirectory) {
        super(configDirectory, "colossalColosseum.json", ColossalColosseumConfig.class);
    }

    @Override
    protected ColossalColosseumConfig getConfig() {
        return null;
    }

    @Override
    protected boolean configIsValid(@Nullable ColossalColosseumConfig config) throws IllegalArgumentException {
        return false;
    }

    @Override
    protected void setConfig(ColossalColosseumConfig config) throws IllegalArgumentException {

    }

    @Override
    protected InputStream getExampleResourceStream() {
        return null;
    }
}
