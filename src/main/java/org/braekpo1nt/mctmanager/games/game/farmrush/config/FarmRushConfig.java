package org.braekpo1nt.mctmanager.games.game.farmrush.config;

import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.farmrush.Arena;
import org.braekpo1nt.mctmanager.games.game.farmrush.ItemSale;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.specs.AnimalGrowerSpec;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.specs.CropGrowerSpec;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.BookMeta;
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
    private boolean clearArenas;
    private boolean buildArenas;
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
    /**
     * The maximum number of points a team can acquire before the game is over. 
     * If a team reaches this number, the game will end. If this is less than 1, 
     * then no maximum score is set and the game will end after the time runs out.
     * Defaults to -1.
     */
    private int maxScore;
    /**
     * If {@link #maxScore} is 1 or more (meaning a max score is assigned) 
     * then the team who reaches the {@link #maxScore} first will receive this bonus. 
     * Can't be negative. Defaults to 0. 
     */
    private int winnerBonus;
    
    /**
     * TODO: remove this and associated functionality when MockBukkit implements {@link BookMeta#toBuilder()}
     * This allows tests to not provide the book. Not to be used by the real game.
     */
    private boolean doNotGiveBookDebug;
    
    private @NotNull CropGrowerSpec cropGrowerSpec;
    
    private @NotNull AnimalGrowerSpec animalGrowerSpec;
    
    public boolean shouldEnforceMaxScore() {
        return maxScore >= 1;
    }
    
    public boolean shouldClearArenas() {
        return clearArenas;
    }
    
    public boolean shouldBuildArenas() {
        return buildArenas;
    }
    
}
