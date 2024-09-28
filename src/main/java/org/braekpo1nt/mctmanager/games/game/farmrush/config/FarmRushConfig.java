package org.braekpo1nt.mctmanager.games.game.farmrush.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.farmrush.Arena;
import org.braekpo1nt.mctmanager.games.game.farmrush.ItemSale;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.Powerup;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.specs.CropGrowerSpec;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.specs.PowerupSpec;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class FarmRushConfig {
    private World world;
    private Location adminLocation;
    private Component description;
    private int descriptionDuration;
    private int startingDuration;
    private int gameDuration;
    private int gameOverDuration;
    private @Nullable ItemStack[] loadout;
    private @Nullable ItemStack[] starterChestContents;
    /**
     * Recipes to enable at the start of the game, and disable at the end of the game
     */
    private @NotNull List<Recipe> recipes;
    /**
     * The NamespacedKeys of the {@link #recipes}. Allows you to remove them at the
     * end of the game.
     */
    private @NotNull List<NamespacedKey> recipeKeys;
    /**
     * The first arena placement
     */
    private Arena firstArena;
    /**
     * The file location of the arena, relative to the Plugin's data folder
     */
    private String arenaFile;
    private @NotNull List<Material> preventInteractions;
    /**
     * The score values that materials are sold for
     */
    private @NotNull Map<Material, ItemSale> materialScores;
    
    private @Nullable ItemStack materialBook;
    
    private @Nullable CropGrowerSpec cropGrowerSpec;
    
}
