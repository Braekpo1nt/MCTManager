package org.braekpo1nt.mctmanager.games.game.farmrush;

import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

@Data
public class ItemSale {
    private final @NotNull Material material;
    private final int requiredAmount;
    private final int score;
    
    public Component toScoreLore(double multiplier) {
        if (requiredAmount > 1) {
            return Component.empty()
                    .append(Component.text("Price: "))
                    .append(Component.text((int) (score * multiplier)))
                    .append(Component.text(" per "))
                    .append(Component.text(requiredAmount))
                    .color(NamedTextColor.GOLD);
        }
        return Component.empty()
                .append(Component.text("Price: "))
                .append(Component.text((int) (score * multiplier)))
                .color(NamedTextColor.GOLD);
    }
}
