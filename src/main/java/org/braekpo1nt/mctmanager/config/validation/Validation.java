package org.braekpo1nt.mctmanager.config.validation;

import org.braekpo1nt.mctmanager.config.ConfigDTO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Validation {
    private Validation() {
        // do not instantiate
    }
    
    public static void validate(boolean expression, String invalidMessage) {
        if (!expression) {
            throw new ConfigInvalidException(invalidMessage);
        }
    }
    
    public static void validate(String name, boolean expression, String invalidMessage) {
        if (!expression) {
            throw new ConfigInvalidException(invalidMessage);
        }
    }
    
    public static void validate(@Nullable ConfigDTO configDTO) {
        validate(configDTO != null, "can't be null");
        configDTO.isValid();
    }
    
    public static void validate(@NotNull List<ConfigDTO> configDTOs) {
        for (int i = 0; i < configDTOs.size(); i++) {
            ConfigDTO configDTO = configDTOs.get(i);
            configDTO.isValid();
        }
    }
    
    public static void validate(boolean expression, String invalidMessage, Object... args) {
        if (!expression) {
            throw new ConfigInvalidException(invalidMessage, args);
        }
    }
}
