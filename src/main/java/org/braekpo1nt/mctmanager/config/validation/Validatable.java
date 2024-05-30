package org.braekpo1nt.mctmanager.config.validation;

import org.jetbrains.annotations.NotNull;

/**
 * An interface for an object which can be validated
 */
public interface Validatable {
    
    void validate(@NotNull Validator validator);
}
