package org.braekpo1nt.mctmanager.config.validation;

import lombok.Getter;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Used to validate the fields and state of an object, 
 * and provide comprehensive, user-centric error reporting when the state is invalid.
 * @see Validatable
 * @see ConfigInvalidException
 */
@Getter
public class Validator {
    
    /**
     * The path of objects at the current level which this validator is validating
     */
    private final String path;
    
    @Override
    public String toString() {
        return path;
    }
    
    public Validator() {
        this.path = "";
    }
    
    public Validator(String path) {
        this.path = path;
    }
    
    /**
     * Validates the given expression.
     * @param expression the expression to evaluate. If this is true, nothing happens. If this is false, a {@link ConfigInvalidException} is thrown with the full path appended to the given message.
     * @param invalidMessage the message to report if the expression is false. This will be appended to the path of this {@link Validator}.
     * @throws ConfigInvalidException if the expression is false. The exception message will include the full path of this {@link Validator} plus the provided invalidMessage
     */
    @Contract("false, _ -> fail")
    public void validate(boolean expression, String invalidMessage) throws ConfigInvalidException {
        if (!expression) {
            throw new ConfigInvalidException(this.path + "." + invalidMessage);
        }
    }
    
    /**
     * 
     * @param expression the expression to evaluate. 
     * @param invalidMessage the message to report if the expression is false. 
     *                       Must be a valid {@link String#format(String, Object...)} string. 
     *                       The provided args will be used as the {code Object...} 
     *                       arguments of the format string.
     * @param args the args the arguments of the {@link String#format(String, Object...)} which uses the invalidMessage as the pattern.
     * @see Validator#validate(boolean, String) 
     */
    @Contract("false, _, _ -> fail")
    public void validate(boolean expression, String invalidMessage, Object... args) {
        validate(expression, String.format(invalidMessage, args));
    }
    
    /**
     * 
     * @param object the object to check if it is null. If this is null, will throw a {@link ConfigInvalidException} with the full path, the objectName, plus a " can't be null" affirmation. If this is not null, nothing happens.
     * @param objectName the name of the object which should not be null
     * @throws ConfigInvalidException if the given object is null
     */
    @Contract("null, _ -> fail")
    public void notNull(Object object, String objectName) {
        if (object == null) {
            throw new ConfigInvalidException(this.path + "." + objectName + " can't be null");
        }
    }
    
    /**
     * An overload of {@link Validator#notNull(Object, String)} which treats the given subPath as a {@link String#format(String, Object...)} argument, and the given args as the arguments to the format.
     * @see Validator#notNull(Object, String) 
     */
    @Contract("null, _, _ -> fail")
    public void notNull(Object object, String subPath, Object... args) {
        if (object == null) {
            throw new ConfigInvalidException(this.path + "." + String.format(subPath, args) + " can't be null");
        }
    }
    
    /**
     * Convenience method to validate that a file specified by a string exists
     * and can be read
     * @param filePath the file path to check for
     * @param subPath the subPath, usually the name of the variable holding the filePath
     * @param args any args to be used as the {@link String#format(String, Object...)} arguments for the subPath string
     */
    public void fileExists(@NotNull String filePath, String subPath, Object... args) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new ConfigInvalidException(this.path + "." + String.format(subPath, args) + " specifies a file that does not exist: " + filePath);
        }
        if (!file.canRead()) {
            throw new ConfigInvalidException(this.path + "." + String.format(subPath, args) + " specifies a file that cannot be read: " + filePath);
        }
    }
    
    /**
     * Convenience method to validate every element of a List of {@link Validatable} objects
     * @param validatables the list of {@link Validatable} objects to validate. 
     * @param listName the name of the list
     * @throws ConfigInvalidException if any element of the list is invalid or null
     */
    public void validateList(@NotNull List<? extends Validatable> validatables, String listName) {
        for (int i = 0; i < validatables.size(); i++) {
            Validatable validatable = validatables.get(i);
            this.notNull(validatable, "%s[%d]", listName, i);
            validatable.validate(this.path("%s[%d]", listName, i));
        }
    }
    
    /**
     * Behaves exactly like {@link Validator#validateList(List, String)}, but does not throw a {@link ConfigInvalidException} if any of the elements are null. Instead, it skips over null elements.
     * @see Validator#validateList(List, String) 
     * @throws ConfigInvalidException if any elements of the list are invalid
     */
    public void validateListNullable(@NotNull List<? extends Validatable> validatables, String listName) {
        for (int i = 0; i < validatables.size(); i++) {
            Validatable validatable = validatables.get(i);
            if (validatable != null) {
                validatable.validate(this.path("%s[%d]", listName, i));
            }
        }
    }
    
    /**
     * Convenience method to validate every value of a Map of {@link Validatable} objects
     * @param validatables the map of {@link Validatable} objects to validate. 
     * @param mapName the name of the map
     * @throws ConfigInvalidException if any value of the map is invalid or null
     */
    public void validateMap(@NotNull Map<?, ? extends Validatable> validatables, String mapName) {
        for (Map.Entry<?, ? extends Validatable> entry : validatables.entrySet()) {
            Validatable validatable = entry.getValue();
            this.notNull(validatable, "%s[%s]", mapName, entry.getKey());
            validatable.validate(this.path("%s[%s]", mapName, entry.getKey()));
        }
    }
    
    /**
     * Behaves exactly like {@link Validator#validateMap(Map, String)}, but does not throw a {@link ConfigInvalidException} if any of the values are null. Instead, it skips over null values.
     * @see Validator#validateMap(Map, String)
     * @throws ConfigInvalidException if any values of the map are invalid
     */
    public void validateMapNullable(@NotNull Map<?, ? extends Validatable> validatables, String mapName) {
        for (Map.Entry<?, ? extends Validatable> entry : validatables.entrySet()) {
            Validatable validatable = entry.getValue();
            if (validatable != null) {
                validatable.validate(this.path("%s[%s]", mapName, entry.getKey()));
            }
        }
    }
    
    /**
     * Append the given subPath to the current path. Use this when going a level deeper into validation.
     * @param subPath the name of the deeper level of validation. Most often this will be the name of the field you are validating.
     * @return a new {@link Validator} with the path of this validator plus the given subPath
     */
    public Validator path(String subPath) {
        if (this.path.isEmpty()) {
            return new Validator(subPath);
        }
        return new Validator(this.path + "." + subPath);
    }
    
    /**
     * An overload of {@link Validator#path(String)} which treats the given subPath as a {@link String#format(String, Object...)} argument, and the given args as the arguments to the format.
     * @see Validator#path(String) 
     */
    public Validator path(String subPath, Object... args) {
        if (this.path.isEmpty()) {
            return new Validator(String.format(subPath, args));
        }
        return new Validator(this.path + "." + String.format(subPath, args));
    }
    
    /**
     * Used when a direct invalid exception is needed, such as in a catch clause.
     * @param subPath the subPath, usually the name of the variable holding the filePath
     * @param args any args to be used as the {@link String#format(String, Object...)} arguments for the subPath string
     * @throws ConfigInvalidException always
     */
    public void invalid(String subPath, Object... args) {
        throw new ConfigInvalidException(this.path + "." + String.format(subPath, args));
    }
}
