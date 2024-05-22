package org.braekpo1nt.mctmanager.config.validation;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Validator {
    
    private String path;
    
    public Validator() {
        this.path = "";
    }
    
    public Validator(String path) {
        this.path = path;
    }
    
    @Contract("false, _ -> fail")
    public void validate(boolean expression, String invalidMessage) {
        if (!expression) {
            throw new ConfigInvalidException(this.path + "." + invalidMessage);
        }
    }
    
    @Contract("null -> fail")
    public void validate(@Nullable Validatable validatable) {
        validate(validatable != null, "can't be null");
        validatable.validate(this);
    }
    
    @Contract("_, false, _ -> fail")
    public void validate(String name, boolean expression, String invalidMessage) {
        if (!expression) {
            throw new ConfigInvalidException(invalidMessage);
        }
    }
    
    @Contract("false, _, _ -> fail")
    public void validate(boolean expression, String invalidMessage, Object... args) {
        if (!expression) {
            throw new ConfigInvalidException(invalidMessage, args);
        }
    }
    
    public Validator path(String subPath) {
        this.path = this.path + "." + subPath;
        return this;
    }
    
    public void validate(@NotNull List<Validatable> validatables) {
        for (int i = 0; i < validatables.size(); i++) {
            Validatable validatable = validatables.get(i);
            validatable.validate(this.path(String.format("[%d]", i)));
        }
    }
}
