package org.braekpo1nt.mctmanager.config.exceptions;

public class ConfigIOException extends ConfigException {
    
    public ConfigIOException(String message) {
        super(message);
    }
    
    public ConfigIOException(String message, Exception cause) {
        super(message, cause);
    }
    
}
