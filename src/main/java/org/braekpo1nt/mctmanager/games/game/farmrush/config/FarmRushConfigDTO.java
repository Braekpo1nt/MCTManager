package org.braekpo1nt.mctmanager.games.game.farmrush.config;

import com.google.common.base.Preconditions;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.LocationDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.ChestInventoryDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.PlayerInventoryDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.RecipeDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.braekpo1nt.mctmanager.games.game.farmrush.ItemSale;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Data
class FarmRushConfigDTO implements Validatable {
    
    private String version;
    private String world;
    private LocationDTO adminLocation;
    /**
     * the file location of the arena, relative to the MCTManager plugin's data folder 
     * ({@code <server>/plugins/MCTManager/}). Example: {@code "farmRushArena.schem"}.
     */
    private String arenaFile;
    /**
     * The location in the world where the first arena will be placed. Will be the
     * minimum position used in WorldEdit's placement of the schematic file.
     * All positions in the {@link #arena} will have this vector added to them.<br>
     * Defaults to {@code (0, 0, 0)}
     */
    private @Nullable Vector firstArenaOrigin;
    /**
     * The items in the starter chest. Can contain no more than 27 entries. 
     */
    private @Nullable ChestInventoryDTO starterChestContents;
    /**
     * Whether to clear the arenas at the end of the game. If set to false,
     * then arenas will still be present at the end of the game.
     * Items will not be removed, mobs will not be killed. 
     * This does not prevent those arenas from being overwritten when a new
     * game starts. See {@link #buildArenas} for that feature.
     * Defaults to true.
     */
    private @Nullable Boolean clearArenas;
    /**
     * Whether to use WorldEdit to build the arenas (from the schematic file)
     * at the start of the game. 
     * Defaults to true.
     * If set to false, this will assume that arenas
     * are build to the required specification to match what would have been
     * placed there on their own.
     * Note that, if false, the starter chest, delivery box, and glass barrier
     * will still be placed. 
     * Note that, if false, if there are 3 physical arenas in the pre-built 
     * world, and 4 teams join, the 4th team will be spawned in the void 
     * and fall to their deaths. 
     */
    private @Nullable Boolean buildArenas;
    /**
     * The details of the arena. When generating arenas for the game, the first one placed down will have its origin at {@link #firstArenaOrigin}, and successive ones will be placed on a grid according to the defined size. Please ensure that the {@link ArenaDTO#getSize()} attribute is the correct dimensions of the {@link #arenaFile} schematic file's dimensions. 
     */
    private ArenaDTO arena;
    private @Nullable List<Material> preventInteractions;
    /**
     * each participant's starting inventory. 
     */
    private @Nullable PlayerInventoryDTO loadout;
    private Scores scores;
    private Durations durations;
    private Component description;
    /**
     * Recipes to enable at the start of the game, and disable at the end of the game
     */
    private @Nullable List<RecipeDTO> recipes;
    /**
     * TODO: remove this and associated functionality when MockBukkit implements {@link BookMeta#toBuilder()}
     * This allows tests to not provide the book. Not to be used by the real game.
     */
    private boolean doNotGiveBookDebug = false;
    /**
     * Details about the powerups
     */
    private @NotNull PowerupData powerups;
    
    @Data
    static class PowerupData implements Validatable {
        
        private @NotNull CropGrowerSpecDTO cropGrower;
        
        private @NotNull AnimalGrowerSpecDTO animalGrower;
        
        @Override
        public void validate(@NotNull Validator validator) {
            validator.notNull(cropGrower, "cropGrower");
            cropGrower.validate(validator.path("cropGrower"));
            validator.notNull(animalGrower, "animalGrower");
            animalGrower.validate(validator.path("animalGrower"));
        }
    }
    
    @Data
    static class Durations {
        private int description = 10;
        private int starting = 10;
        /**
         * how long the players have to farm rush, in seconds
         */
        private int gameDuration = 180;
        private int gameOver = 10;
    }
    
    @Data
    static class Scores implements Validatable {
        /**
         * The maximum number of points a team can acquire before the game is over. 
         * If a team reaches this number, the game will end. If this is less than 1, 
         * then no maximum score is set and the game will end after the time runs out.
         * Defaults to -1.
         */
        private int maxScore = -1;
        /**
         * If {@link #maxScore} is 1 or more (meaning a max score is assigned) 
         * then the team who reaches the {@link #maxScore} first will receive this bonus. 
         * Can't be negative. Defaults to 0. 
         */
        private int winnerBonus = 0;
        private Map<Material, ItemSale> materialScores;
        
        @Override
        public void validate(@NotNull Validator validator) {
            validator.notNull(materialScores, "materialScores");
            validator.validateMap(materialScores, "materialScores");
            validator.validate(winnerBonus >= 0, "winnerBonus can't be negative");
        }
    }
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(this.getVersion(), "version");
        validator.validate(Main.VALID_CONFIG_VERSIONS.contains(this.getVersion()), "invalid config version (%s)", this.getVersion());
        validator.notNull(Bukkit.getWorld(this.getWorld()), "Could not find world \"%s\"", this.getWorld());
        validator.notNull(adminLocation, "adminLocation");
        validator.notNull(arenaFile, "arenaFile");
        validator.notNull(firstArenaOrigin, "firstArenaOrigin");
        validator.notNull(starterChestContents, "starterChestContents");
        starterChestContents.validate(validator.path("starterChestContents"));
        validator.notNull(arena, "arena");
        arena.validate(validator.path("arena"));
        if (loadout != null) {
            loadout.validate(validator.path("loadout"));
        }
        validator.notNull(scores, "scores");
        scores.validate(validator.path("scores"));
        validator.notNull(this.getDurations(), "durations");
        validator.validate(this.getDurations().getDescription() >= 0, "durations.description (%s) can't be negative", this.getDurations().getDescription());
        validator.validate(this.getDurations().getStarting() >= 0, "durations.starting (%s) can't be negative", this.getDurations().getStarting());
        validator.validate(this.getDurations().getGameDuration() >= 0, "durations.gameDuration (%s) can't be negative", this.getDurations().getGameDuration());
        validator.validate(this.getDurations().getGameOver() >= 0, "durations.gameOver (%s) can't be negative", this.getDurations().getGameOver());
        validator.notNull(this.getDescription(), "description");
        if (recipes != null) {
            validator.validateList(recipes, "recipes");
        }
        validator.notNull(powerups, "powerups");
        powerups.validate(validator.path("powerups"));
    }
    
    public FarmRushConfig toConfig() {
        World newWorld = Bukkit.getWorld(this.world);
        Preconditions.checkState(newWorld != null, "Could not find world \"%s\"", this.world);
        ItemStack[] newStarterChestContents = null;
        ItemStack[] newLoadout = null;
        if (this.starterChestContents != null) {
            newStarterChestContents = this.starterChestContents.toInventoryContents();
            for (ItemStack item : newStarterChestContents) {
                addScoreLore(item, scores.getMaterialScores());
            }
        }
        if (this.loadout != null) {
            newLoadout = this.loadout.toInventoryContents();
            for (ItemStack item : newLoadout) {
                addScoreLore(item, scores.getMaterialScores());
            }
        }
        return FarmRushConfig.builder()
                .world(newWorld)
                .adminLocation(this.adminLocation.toLocation(newWorld))
                .description(this.description)
                .arenaFile(this.arenaFile)
                .clearArenas(this.clearArenas == null || this.clearArenas)
                .buildArenas(this.buildArenas == null || this.buildArenas)
                .firstArena(this.arena.toArena(newWorld).offset(firstArenaOrigin != null ? firstArenaOrigin : new Vector(0, 0, 0)))
                .starterChestContents(newStarterChestContents)
                .loadout(newLoadout)
                .preventInteractions(this.preventInteractions != null ? this.preventInteractions : Collections.emptyList())
                .descriptionDuration(this.durations.description)
                .startingDuration(this.durations.starting)
                .gameDuration(this.durations.gameDuration)
                .gameOverDuration(this.durations.gameOver)
                .materialScores(this.scores.materialScores)
                .maxScore(this.scores.maxScore)
                .winnerBonus(this.scores.winnerBonus)
                .doNotGiveBookDebug(this.doNotGiveBookDebug)
                .recipes(this.recipes != null ? RecipeDTO.toRecipes(this.recipes) : Collections.emptyList())
                .recipeKeys(this.recipes != null ? RecipeDTO.toNamespacedKeys(this.recipes) : Collections.emptyList())
                .cropGrowerSpec(this.powerups.getCropGrower().toSpec(newWorld))
                .animalGrowerSpec(this.powerups.getAnimalGrower().toSpec(newWorld))
                .build();
    }
    
    /**
     * If the given item has a score associated with its Material type in the config,
     * this method adds a line to the item's lore showing how many points it's worth.<br>
     *
     * This is an idempotent operation, meaning running it on the same item twice will
     * result in only 1 score line being added to the lore. It marks items that have been
     * modified with a persistent data container boolean using {@link FarmRushGame#HAS_SCORE_LORE}
     * as the namespaced key.
     *
     * @param item the item to add the score to, if it exists
     */
    private static void addScoreLore(@Nullable ItemStack item, @NotNull Map<Material, ItemSale> materialScores) {
        if (item == null || item.getType().equals(Material.AIR)) {
            return;
        }
        Component scoreLore = getScoreLore(item.getType(), materialScores);
        if (scoreLore == null) {
            return;
        }
        item.editMeta(meta -> {
            if (meta.getPersistentDataContainer().has(FarmRushGame.HAS_SCORE_LORE, PersistentDataType.BOOLEAN)) {
                return;
            }
            List<Component> originalLore = meta.lore();
            if (originalLore == null) {
                meta.lore(Collections.singletonList(scoreLore));
            } else {
                List<Component> newLore = new ArrayList<>(originalLore);
                newLore.add(scoreLore);
                meta.lore(newLore);
            }
            meta.getPersistentDataContainer().set(FarmRushGame.HAS_SCORE_LORE, PersistentDataType.BOOLEAN, true);
        });
    }
    
    /**
     * @param type the type to get the score lore of
     * @return the lore line describing how many points the given item type is worth.
     * null if the given type is not listed in the config.
     */
    private static @Nullable Component getScoreLore(@Nullable Material type, Map<Material, ItemSale> materialScores) {
        if (type == null) {
            return null;
        }
        ItemSale score = materialScores.get(type);
        if (score == null) {
            return null;
        }
        return Component.empty()
                .append(Component.text("Price: "))
                .append(Component.text(score.getScore()))
                .color(NamedTextColor.GOLD);
    }
}
