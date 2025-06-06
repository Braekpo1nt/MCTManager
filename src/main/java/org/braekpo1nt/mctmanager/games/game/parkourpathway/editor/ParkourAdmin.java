package org.braekpo1nt.mctmanager.games.game.parkourpathway.editor;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.braekpo1nt.mctmanager.games.editor.Admin;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@ToString(callSuper = true)
@Getter
@Setter
public class ParkourAdmin extends Admin {
    
    private Display display;
    private int currentPuzzle;
    private int currentInBound;
    private int currentCheckPoint;
    
    public ParkourAdmin(@NotNull Player player) {
        super(player);
    }
}
