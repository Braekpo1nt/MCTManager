package org.braekpo1nt.mctmanager.config.validation;

import lombok.ToString;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@ToString
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
    
    @Contract("null, _ -> fail")
    public void notNull(Object object, String subPath) {
        if (object == null) {
            throw new ConfigInvalidException(this.path + "." + subPath + " can't be null");
        }
    }
    
    @Contract("false, _, _ -> fail")
    public void validate(boolean expression, String invalidMessage, Object... args) {
        if (!expression) {
            throw new ConfigInvalidException(invalidMessage, args);
        }
    }
    
    public Validator path(String subPath) {
        if (this.path.isEmpty()) {
            return new Validator(subPath);
        }
        return new Validator(this.path + "." + subPath);
    }
    
    public Validator path(String subPath, Object... args) {
        if (this.path.isEmpty()) {
            return new Validator(String.format(subPath, args));
        }
        return new Validator(this.path + "." + String.format(subPath, args));
    }
}
