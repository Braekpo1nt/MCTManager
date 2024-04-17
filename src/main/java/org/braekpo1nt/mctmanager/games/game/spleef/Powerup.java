package org.braekpo1nt.mctmanager.games.game.spleef;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
@AllArgsConstructor
public class Powerup {
    public enum Type {
        PLAYER_SWAPPER,
        BLOCK_BREAKER,
        SHIELD,
    }
    
    private final ItemStack item;
    private final Type type;
}
