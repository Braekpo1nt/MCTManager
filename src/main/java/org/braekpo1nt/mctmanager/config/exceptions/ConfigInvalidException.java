package org.braekpo1nt.mctmanager.config.exceptions;

import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;

public class ConfigInvalidException extends ConfigException {
    
    public ConfigInvalidException(String message) {
        super(message);
    }
    
    public ConfigInvalidException(String message, Exception cause) {
        super(message, cause);
    }
    
}
