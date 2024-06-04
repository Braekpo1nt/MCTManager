package org.braekpo1nt.mctmanager.config.exceptions;

public class ConfigInvalidException extends ConfigException {
    
    public ConfigInvalidException(String message) {
        super(message);
    }
    
    public ConfigInvalidException(String message, Exception cause) {
        super(message, cause);
    }
    
}
