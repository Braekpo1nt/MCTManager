package org.braekpo1nt.mctmanager.config.exceptions;

public class ConfigException extends RuntimeException {
    
    public ConfigException(String message) {
        super(message);
    }
    
    public ConfigException(String message, Exception cause) {
        super(message, cause);
    }
    
}
