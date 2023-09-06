package org.braekpo1nt.mctmanager.games.event.config;

import com.google.common.base.Preconditions;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;

public class EventStorageUtil extends GameConfigStorageUtil<EventConfig> {
    
    private EventConfig eventConfig;
    
    /**
     * @param configDirectory The directory that the config should be located in (e.g. the plugin's data folder)
     */
    public EventStorageUtil(File configDirectory) {
        super(configDirectory, "eventConfig.json", EventConfig.class);
    }
    
    @Override
    protected EventConfig getConfig() {
        return eventConfig;
    }
    
    @Override
    protected boolean configIsValid(@Nullable EventConfig config) throws IllegalArgumentException {
        Preconditions.checkArgument(config != null, "config is null");
        Preconditions.checkArgument(config.version() != null, "version can't be null");
        Preconditions.checkArgument(config.version().equals(Main.CONFIG_VERSION), "version %s doesn't match required config version %s", config.version(), Main.CONFIG_VERSION);
        Preconditions.checkArgument(config.title() != null, "title can't be null");
        Preconditions.checkArgument(config.title().length() >= 1, "title must be at least 1 character");
        Preconditions.checkArgument(config.multipliers() != null, "multipliers can't be null");
        Preconditions.checkArgument(config.multipliers().length >= 1, "there must be at least 1 multiplier");
        Preconditions.checkArgument(config.durations() != null, "durations can't be null");
        Preconditions.checkArgument(config.durations().waitingInHub() >= 0, "durations.waitingInHub can't be negative");
        Preconditions.checkArgument(config.durations().halftimeBreak() >= 0, "durations.halftimeBreak can't be negative");
        Preconditions.checkArgument(config.durations().voting() >= 0, "durations.voting can't be negative");
        Preconditions.checkArgument(config.durations().startingGame() >= 0, "durations.startingGame can't be negative");
        Preconditions.checkArgument(config.durations().backToHub() >= 0, "durations.backToHub can't be negative");
        return true;
    }
    
    @Override
    protected void setConfig(EventConfig config) throws IllegalArgumentException {
        this.eventConfig = config;
    }
    
    @Override
    protected InputStream getExampleResourceStream() {
        return EventStorageUtil.class.getResourceAsStream("exampleEventConfig.json");
    }
    
    public int getWaitingInHubDuration() {
        return eventConfig.durations().waitingInHub();
    }
    
    public int getHalftimeBreakDuration() {
        return eventConfig.durations().halftimeBreak();
    }
    
    public int getVotingDuration() {
        return eventConfig.durations().voting();
    }
    
    public int getStartingGameDuration() {
        return eventConfig.durations().startingGame();
    }
    
    public int getBackToHubDuration() {
        return eventConfig.durations().backToHub();
    }
    
    public double[] getMultipliers() {
        return eventConfig.multipliers();
    }
    
    public String getTitle() {
        return eventConfig.title();
    }
}
