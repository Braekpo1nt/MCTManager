package org.braekpo1nt.mctmanager.games.game.spleef.powerup;

import lombok.*;
import net.kyori.adventure.sound.Sound;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@RequiredArgsConstructor
public class Powerup {
    public enum Type {
        PLAYER_SWAPPER,
        BLOCK_BREAKER,
        SHIELD,
    }
    
    private final @NotNull ItemStack item;
    private final @NotNull Type type;
    @Setter
    private @Nullable Sound userSound;
    @Setter
    private @Nullable Sound affectedSound;
    
    @Override
    public String toString() {
        return type.toString();
    }
}
