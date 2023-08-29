package org.braekpo1nt.mctmanager.games.game.capturetheflag.config;

import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;

public class CaptureTheFlagStorageUtil extends GameConfigStorageUtil<CaptureTheFlagConfig> {
    private CaptureTheFlagConfig captureTheFlagConfig = null;
    
    public CaptureTheFlagStorageUtil(File configDirectory) {
        super(configDirectory, "captureTheFlagConfig.json", CaptureTheFlagConfig.class);
    }
    
    @Override
    protected CaptureTheFlagConfig getConfig() {
        return captureTheFlagConfig;
    }
    
    @Override
    protected boolean configIsValid(@Nullable CaptureTheFlagConfig config) throws IllegalArgumentException {
        return false;
    }
    
    @Override
    protected void setConfig(CaptureTheFlagConfig config) {
        this.captureTheFlagConfig = config;
    }
    
    @Override
    protected InputStream getExampleResourceStream() {
        return CaptureTheFlagStorageUtil.class.getResourceAsStream("exampleCaptureTheFlagConfig.json");
    }
}
