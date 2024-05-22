package org.braekpo1nt.mctmanager.config.validation;

public class ConfigInvalidException extends RuntimeException {
    
    public ConfigInvalidException(String message) {
        super(message);
    }
    
    public ConfigInvalidException(String message, Object... args) {
        super(String.format(message, args));
    }
    
}
