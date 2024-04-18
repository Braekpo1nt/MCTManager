package org.braekpo1nt.mctmanager.games.game.spleef;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@AllArgsConstructor
public class Powerup {
    public enum Type {
        PLAYER_SWAPPER,
        BLOCK_BREAKER,
        SHIELD,
    }
    
    private final @NotNull ItemStack item;
    private final @NotNull Type type;
    private final @Nullable Sound userSound;
    private final @Nullable Sound affectedSound;
}
