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
    /**
     * how much time should be left when one team reaches the {@link #maxScore}
     * before the game ends.
     */
    private int gracePeriodDuration;
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
     * The file location of the arena, relative to the Plugin's data folder and farm-rush folder
     * e.g. /home/container/plugins/MCTManager/farm-rush/filename.schem
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
     * The sell cap, beyond which players are not allowed to gain points from selling items.
     * Note that if this is greater than 1 but less than {@link #maxScore}, then players
     * will be unable to reach the max score through selling items. 
     * If this is less than 1, then no score cap is in effect. 
     * Defaults to -1.
     */
    private int sellCap;
    /**
     * the percentage of the {@link #maxScore} that a team can reach before 
     * the other teams are alerted that they are x% of the way there.
     * If this is negative, don't warn.
     */
    private double warningThreshold;
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
    
    public boolean shouldEnforceScoreCap() {
        return sellCap >= 1;
    }
    
    public boolean shouldWarnAtThreshold() {
        return warningThreshold >= 0;
    }
    
    public boolean shouldClearArenas() {
        return clearArenas;
    }
    
    public boolean shouldBuildArenas() {
        return buildArenas;
    }
}
