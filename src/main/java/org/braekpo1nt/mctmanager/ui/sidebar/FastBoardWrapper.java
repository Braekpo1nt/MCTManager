package org.braekpo1nt.mctmanager.ui.sidebar;

import fr.mrmicky.fastboard.adventure.FastBoard;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;

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
            Main.logger().severe(String.format("Critical error, unable to add player to FastBoardWrapper. Failing gracefully, see below for details. \n%s", e.getMessage()));
            e.printStackTrace();
        }
    }
    
    /**
     * Update the scoreboard title.
     *
     * @param title the new scoreboard title
     * @throws IllegalArgumentException if the title is longer than 32 chars on 1.12 or lower
     * @throws IllegalStateException    if {@link #delete()} was call before
     */
    public void updateTitle(String title) {
        board.updateTitle(Component.text(title));
    }
    
    /**
     * Update the scoreboard title.
     *
     * @param title the new scoreboard title
     * @throws IllegalArgumentException if the title is longer than 32 chars on 1.12 or lower
     * @throws IllegalStateException    if {@link #delete()} was call before
     */
    public void updateTitle(Component title) {
        board.updateTitle(title);
    }
    
    /**
     * Update the lines of the scoreboard
     *
     * @param lines the new scoreboard lines
     * @throws IllegalArgumentException if one line is longer than 30 chars on 1.12 or lower
     * @throws IllegalStateException    if {@link #delete()} was call before
     */
    public void updateLines(String... lines) {
        board.updateLines(Arrays.stream(lines).map(line -> (Component) Component.text(line)).toList());
    }
    
    /**
     * Update the lines of the scoreboard
     *
     * @param lines the new scoreboard lines
     * @throws IllegalArgumentException if one line is longer than 30 chars on 1.12 or lower
     * @throws IllegalStateException    if {@link #delete()} was call before
     */
    public void updateLines(Collection<Component> lines) {
        board.updateLines(lines);
    }
    
    /**
     * Update the lines of the scoreboard
     *
     * @param lines the new scoreboard lines
     * @throws IllegalArgumentException if one line is longer than 30 chars on 1.12 or lower
     * @throws IllegalStateException    if {@link #delete()} was call before
     */
    public void updateLines(Component... lines) {
        board.updateLines(lines);
    }
    
    
    /**
     * Update a single scoreboard line.
     *
     * @param line the line number
     * @param text the new line text
     * @throws IndexOutOfBoundsException if the line is higher than {@link #size() size() + 1}
     */
    public void updateLine(int line, String text) {
        board.updateLine(line, Component.text(text));
    }
    
    /**
     * Update a single scoreboard line.
     *
     * @param line the line number
     * @param text the new line text
     * @throws IndexOutOfBoundsException if the line is higher than {@link #size() size() + 1}
     */
    public void updateLine(int line, Component text) {
        board.updateLine(line, text);
    }
    
    /**
     * Get the scoreboard size (the number of lines).
     *
     * @return the size
     */
    public int size() {
        return board.size();
    }
    
    /**
     * Remove a scoreboard line.
     *
     * @param line the line number
     */
    public void removeLine(int line) {
        this.board.removeLine(line);
    }
    
    
    /**
     * Get if the scoreboard is deleted
     * 
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
