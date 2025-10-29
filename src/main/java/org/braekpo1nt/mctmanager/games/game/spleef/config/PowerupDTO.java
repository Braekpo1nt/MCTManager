package org.braekpo1nt.mctmanager.games.game.spleef.config;

import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.dto.net.kyori.adventure.sound.SoundDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.ItemStackDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.spleef.powerup.Powerup;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Data
class PowerupDTO implements Validatable {
    private @Nullable SoundDTO userSound;
    private @Nullable SoundDTO affectedSound;
    /**
     * the {@link Powerup.Source}s which this powerup is restricted to. If all sources are valid, make this null.
     * Otherwise, the powerup will only be given to players from the specified sources. If no sources are valid, make
     * this empty.
     */
    private @Nullable List<Powerup.@Nullable Source> sources;
    /**
     * The ItemStack that represents the powerup
     */
    private ItemStackDTO item;
    
    @Override
    public void validate(@NotNull Validator validator) {
        if (userSound != null) {
            userSound.validate(validator.path("userSound"));
        }
        if (affectedSound != null) {
            affectedSound.validate(validator.path("affectedSound"));
        }
        if (item != null) {
            item.validate(validator.path("item"));
        }
    }
    
    /**
     * a convenience method to filter out null entries from {@link PowerupDTO#sources}.
     * If sources is null, returns a list of all {@link Powerup.Source}s, because on the user's
     * side, unspecified sources indicates all sources are valid.
     * @return a list of the sources which this powerup can come from
     */
    @NotNull List<Powerup.@NotNull Source> getSources() {
        if (sources == null) {
            return Arrays.asList(Powerup.Source.values());
        }
        return sources.stream().filter(Objects::nonNull).toList();
    }
    
    /**
     * @param requiredMaterial the material required
     * @return true if this {@link #item} is null, true if {@link #item} is not null and {@link ItemStackDTO#getType()}
     * equals the requiredMaterial, or false otherwise
     */
    public boolean hasRequiredMaterial(@NotNull Material requiredMaterial) {
        if (item == null) {
            return true;
        }
        if (item.getType() == null) {
            return false;
        }
        return item.getType().equals(requiredMaterial);
    }
    
    /**
     * @param type the type of the powerup
     * @return the powerup
     */
    public @NotNull Powerup toPowerup(@NotNull Powerup.Type type) {
        Powerup.PowerupBuilder builder = Powerup.builder();
        if (userSound != null) {
            builder.userSound(userSound.toSound());
        }
        if (affectedSound != null) {
            builder.affectedSound(affectedSound.toSound());
        }
        return builder
                .item(item != null ? item.toItemStack() : defaultItem(type))
                .type(type)
                .sources(getSources())
                .build();
    }
    
    private static @NotNull ItemStack defaultItem(@NotNull Powerup.Type type) {
        return switch (type) {
            case PLAYER_SWAPPER -> {
                ItemStack playerSwapperItem = new ItemStack(Material.SNOWBALL);
                playerSwapperItem.editMeta(meta -> {
                    meta.displayName(Component.text("Player Swapper"));
                    meta.lore(List.of(
                            Component.text("Throw this at another player"),
                            Component.text("to swap positions with them.")
                    ));
                });
                yield playerSwapperItem;
            }
            case BLOCK_BREAKER -> {
                ItemStack blockBreakerItem = new ItemStack(Material.SNOWBALL);
                blockBreakerItem.editMeta(meta -> {
                    meta.displayName(Component.text("Block Breaker"));
                    meta.lore(List.of(
                            Component.text("Throw this at a block"),
                            Component.text("to break it.")
                    ));
                });
                yield blockBreakerItem;
            }
            case SHIELD -> {
                ItemStack shieldItem = new ItemStack(Material.LAPIS_LAZULI);
                shieldItem.editMeta(meta -> {
                    meta.displayName(Component.text("Swap Shield"));
                    meta.lore(List.of(
                            Component.text("- Activates automatically"),
                            Component.text("- Single use")
                    ));
                });
                yield shieldItem;
            }
        };
    }
}
