package org.braekpo1nt.mctmanager.config;

import org.braekpo1nt.mctmanager.config.validation.ConfigInvalidException;

/**
 * An interface for a ConfigDto
 */
public interface ConfigDTO {
    
    /**
     * Does nothing if the config is valid, throws a {@link ConfigInvalidException} if the config is invalid, with a message about what was invalid
     * @throws ConfigInvalidException if the config is invalid
     */
    void isValid() throws ConfigInvalidException;
}
