package org.braekpo1nt.mctmanager.games.game.footrace.editor;

import lombok.Getter;
import lombok.Setter;
import org.braekpo1nt.mctmanager.games.editor.Admin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class FootRaceAdmin extends Admin {
    
    private int currentCheckpoint;
    
    public FootRaceAdmin(@NotNull Player player) {
        super(player);
        this.currentCheckpoint = 0;
    }
}
