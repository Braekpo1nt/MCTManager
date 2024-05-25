package org.braekpo1nt.mctmanager.games.game.spleef.config;

import org.braekpo1nt.mctmanager.config.ConfigStorageUtil;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.spleef.powerup.Powerup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.*;

public class SpleefStorageUtil extends ConfigStorageUtil<SpleefConfigDTO> {
    
    protected SpleefConfigDTO spleefConfigDTO = null;
    
    protected SpleefConfig spleefConfig = null;
    
    public SpleefStorageUtil(File configDirectory) {
        super(configDirectory, "spleefConfig.json", SpleefConfigDTO.class);
    }
    
    /**
     * 
     * @deprecated in favor of {@link SpleefStorageUtil#getConfig2}
     */
    @Deprecated
    @Override
    protected SpleefConfigDTO getConfig() {
        return spleefConfigDTO;
    }
    
    @Override
    protected boolean configIsValid(@Nullable SpleefConfigDTO config) throws IllegalArgumentException {
        Validator validator = new Validator();
        validator.notNull(config, "spleefConfig");
        config.validate(validator.path("spleefConfig"));
        return true;
    }
    
    @Override
    protected void setConfig(SpleefConfigDTO configDTO) {
        this.spleefConfig = configDTO.toConfig();
    }
    
    /**
     * @return a map of every source to a chance of -1 (i.e. no chance)
     */
    static Map<Powerup.Source, Double> getDefaultChances() {
        Map<Powerup.Source, Double> result = new HashMap<>();
        for (Powerup.Source source : Powerup.Source.values()) {
            result.put(source, -1.0);
        }
        return result;
    }
    
    /**
     * @return a map from every {@link Powerup.Source} to a map of every {@link Powerup.Type} value to a weight of 1
     */
    static @NotNull Map<Powerup.Source, Map<Powerup.Type, Integer>> getDefaultSourcePowerups() {
        Map<Powerup.Type, Integer> weights = new HashMap<>();
        for (Powerup.Type value : Powerup.Type.values()) {
            weights.put(value, 1);
        }
        Map<Powerup.Source, Map<Powerup.Type, Integer>> result = new HashMap<>();
        for (Powerup.Source source : Powerup.Source.values()) {
            result.put(source, weights);
        }
        return result;
    }
    
    @Override
    protected InputStream getExampleResourceStream() {
        return SpleefStorageUtil.class.getResourceAsStream("exampleSpleefConfig.json");
    }
    
    public SpleefConfig getConfig2() {
        return this.spleefConfig;
    }
}
