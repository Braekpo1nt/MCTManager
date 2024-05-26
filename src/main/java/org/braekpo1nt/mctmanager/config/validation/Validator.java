package org.braekpo1nt.mctmanager.config.validation;

import lombok.Getter;
import lombok.ToString;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.jetbrains.annotations.Contract;

@ToString
@Getter
public class Validator {
    
    private final String path;
    
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
    
    @Contract("null, _, _ -> fail")
    public void notNull(Object object, String subPath, Object... args) {
        if (object == null) {
            throw new ConfigInvalidException(this.path + "." + String.format(subPath, args) + " can't be null");
        }
    }
    
    @Contract("false, _, _ -> fail")
    public void validate(boolean expression, String invalidMessage, Object... args) {
        if (!expression) {
            throw new ConfigInvalidException(String.format(invalidMessage, args));
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
