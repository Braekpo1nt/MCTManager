package org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.old;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.braekpo1nt.mctmanager.display.Renderer;
import org.braekpo1nt.mctmanager.games.editor.Admin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@ToString(callSuper = true)
@Getter
@Setter
public class ParkourAdminOld extends Admin {
    
    private Renderer display;
    /**
     * the index of the puzzle this admin is editing
     */
    private int currentPuzzle;
    /**
     * the index of the inBounds box this admin is editing
     */
    private int currentInBound;
    /**
     * The index of the checkpoint that this admin is editing in their 
     * current puzzle (since there can be multiple)
     */
    private int currentCheckPoint;
    
    public ParkourAdminOld(@NotNull Player player) {
        super(player);
    }
}
