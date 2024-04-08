package org.braekpo1nt.mctmanager.commands;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandUtils {
    
    /**
     * @param value the string to check if it is an integer
     * @return true if the string is an integer, false if not
     */
    public static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * @param value the string to check if it is a double
     * @return true if the string is a double, false if not
     */
    public static boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * @param value the string to check if it is a float
     * @return true if the string is a float, false if not
     */
    public static boolean isFloat(String value) {
        try {
            Float.parseFloat(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * @param value the string to be parsed
     * @return the boolean value the string represents if the string can be successfully parsed to a boolean, null if the string couldn't be parsed to a boolean
     */
    public static @Nullable Boolean toBoolean(@NotNull String value) {
        String lowerCase = value.toLowerCase();
        switch (lowerCase) {
            case "true", "yes", "t", "y", "1" -> {
                return true;
            }
            case "false", "no", "f", "n", "0" -> {
                return false;
            }
            default -> {
                return null;
            }
        }
    }
}
