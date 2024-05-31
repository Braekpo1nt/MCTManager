package org.braekpo1nt.mctmanager.config.validation;

import org.jetbrains.annotations.NotNull;

/**
 * An interface for an object which can be validated with a {@link Validator}
 * @see Validator
 */
public interface Validatable {
    
    /**
     * Validate this object using the provided {@link Validator}
     * @param validator the {@link Validator} to use in validating this object
     */
    void validate(@NotNull Validator validator);
}
