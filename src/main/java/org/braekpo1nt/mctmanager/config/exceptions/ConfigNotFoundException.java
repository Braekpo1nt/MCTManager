package org.braekpo1nt.mctmanager.config.exceptions;

import java.io.File;

public class ConfigNotFoundException extends ConfigIOException {
    /**
     * @param file the file that could not be found
     */
    public ConfigNotFoundException(File file) {
        super(String.format("Could not find config file \"%s\"", file));
    }
}
