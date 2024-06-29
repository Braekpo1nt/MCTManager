package org.braekpo1nt.mctmanager.ui.sidebar;

import fr.mrmicky.fastboard.adventure.FastBoard;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * A wrapper for the FastBoard class that is Mockable. This is necessary
 * because FastBoard can't be mocked because it was not designed with testing
 * in mind. 
 */
public class FastBoardWrapper {
    
    protected FastBoard board = null;
    
    /**
     * This instantiates a new FastBoard internally using the given player
     * as the argument to the constructor.
     * @param player the owner of the scoreboard
     */
    public void setPlayer(Player player) {
        try {
            this.board = new FastBoard(player);
        } catch (ExceptionInInitializerError e) {
            Bukkit.getLogger().severe(String.format("Critical error, unable to add player to FastBoardWrapper. Failing gracefully, see below for details. \n%s", e.getMessage()));
            e.printStackTrace();
        }
    }
    
    /**
     * Update the scoreboard title
     * @param title the title
     */
    public void updateTitle(String title) {
        board.updateTitle(Component.text(title));
    }
    
    /**
     * Update all the scoreboard lines
     * @param lines
     */
    public void updateLines(String... lines) {
        board.updateLines(Arrays.stream(lines).map(line -> (Component) Component.text(line)).toList());
    }
    
    /**
     * Update a single scoreboard line
     * @param line
     * @param text
     */
    public void updateLine(int line, String text) {
        board.updateLine(line, Component.text(text));
    }
    
    
    /**
     * Get if the scoreboard is deleted
     * @return true if the scoreboard is deleted
     */
    public boolean isDeleted() {
        return board.isDeleted();
    }
    
    /**
     * Delete this FastBoard, and will remove the scoreboard for the associated player if they are online. 
     * After this, all uses of updateLines and updateTitle will throw an IllegalStateException
     * @throws IllegalStateException – if this was already call before
     */
    public void delete() {
        board.delete();
    }
    
}
