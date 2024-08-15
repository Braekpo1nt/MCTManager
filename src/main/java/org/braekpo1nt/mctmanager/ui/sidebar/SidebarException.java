package org.braekpo1nt.mctmanager.ui.sidebar;

/**
 * Meant to be thrown (then caught and reported gracefully) when there is an error with the Sidebar.
 */
public class SidebarException extends RuntimeException {
    public SidebarException(String message) {
        super(message);
    }
}
