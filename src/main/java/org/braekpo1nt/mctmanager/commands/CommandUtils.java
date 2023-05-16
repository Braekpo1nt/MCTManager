package org.braekpo1nt.mctmanager.commands;

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
    
    
}
