package org.braekpo1nt.mctmanager.commands;

import org.braekpo1nt.mctmanager.games.GameManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CommandUtils {
    
    /**
     * @param value the string to check if it is an integer
     * @return true if the string is an integer, false if not
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isInteger(@NotNull String value) {
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
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isDouble(@NotNull String value) {
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
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isFloat(@NotNull String value) {
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
    
    public static @NotNull List<String> partialMatchTabList(@NotNull List<@NotNull String> list, @Nullable String partial) {
        if (partial == null || partial.isEmpty()) {
            return list;
        }
        String lowerCasePartial = partial.toLowerCase();
        return list.stream().filter(s -> s.toLowerCase().startsWith(lowerCasePartial)).toList();
    }
}
