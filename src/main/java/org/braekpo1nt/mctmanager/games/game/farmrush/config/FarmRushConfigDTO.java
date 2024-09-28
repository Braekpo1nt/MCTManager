package org.braekpo1nt.mctmanager.games.game.farmrush.config;

import com.google.common.base.Preconditions;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.LocationDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.ChestInventoryDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.PlayerInventoryDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.recipes.RecipeDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.braekpo1nt.mctmanager.games.game.farmrush.ItemSale;
import org.braekpo1nt.mctmanager.games.game.farmrush.powerups.specs.CropGrowerSpec;
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
     * The details of the arena. When generating arenas for the game, the first one placed down will have its origin at {@link #firstArenaOrigin}, and successive ones will be placed on a grid according to the defined size. Please ensure that the {@link ArenaDTO#getSize()} attribute is the correct dimensions of the {@link #arenaFile} schematic file's dimensions. 
     */
    private ArenaDTO arena;
    private @Nullable List<Material> preventInteractions;
    /**
     * each participant's starting inventory. 
     */
    private @Nullable PlayerInventoryDTO loadout;
    private @Nullable Map<Material, ItemSale> materialScores;
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
    private @Nullable PowerupData powerups;
    
    @Data
    static class PowerupData implements Validatable {
        private CropGrowerSpecDTO cropGrower;
        
        @Override
        public void validate(@NotNull Validator validator) {
            if (cropGrower != null) {
                cropGrower.validate(validator.path("cropGrower"));
            }
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
        if (materialScores != null) {
            validator.validateMap(materialScores, "materialScores");
        }
        validator.notNull(this.getDurations(), "durations");
        validator.validate(this.getDurations().getDescription() >= 0, "durations.description (%s) can't be negative", this.getDurations().getDescription());
        validator.validate(this.getDurations().getStarting() >= 0, "durations.starting (%s) can't be negative", this.getDurations().getStarting());
        validator.validate(this.getDurations().getGameDuration() >= 0, "durations.gameDuration (%s) can't be negative", this.getDurations().getGameDuration());
        validator.validate(this.getDurations().getGameOver() >= 0, "durations.gameOver (%s) can't be negative", this.getDurations().getGameOver());
        validator.notNull(this.getDescription(), "description");
        if (recipes != null) {
            validator.validateList(recipes, "recipes");
        }
    }
    
    public FarmRushConfig toConfig() {
        World newWorld = Bukkit.getWorld(this.world);
        Preconditions.checkState(newWorld != null, "Could not find world \"%s\"", this.world);
        ItemStack[] newStarterChestContents = null;
        ItemStack[] newLoadout = null;
        if (materialScores != null) {
            if (this.starterChestContents != null) {
                newStarterChestContents = this.starterChestContents.toInventoryContents();
                for (ItemStack item : newStarterChestContents) {
                    addScoreLore(item, materialScores);
                }
            }
            if (this.loadout != null) {
                newLoadout = this.loadout.toInventoryContents();
                for (ItemStack item : newLoadout) {
                    addScoreLore(item, materialScores);
                }
            }
        }
        CropGrowerSpec newCropGrowerSpec;
        if (powerups != null) {
            newCropGrowerSpec = powerups.getCropGrower().toSpec();
        } else {
            newCropGrowerSpec = null;
        }
        return FarmRushConfig.builder()
                .world(newWorld)
                .adminLocation(this.adminLocation.toLocation(newWorld))
                .description(this.description)
                .arenaFile(this.arenaFile)
                .firstArena(this.arena.toArena(newWorld).offset(firstArenaOrigin != null ? firstArenaOrigin : new Vector(0, 0, 0)))
                .starterChestContents(newStarterChestContents)
                .loadout(newLoadout)
                .preventInteractions(this.preventInteractions != null ? this.preventInteractions : Collections.emptyList())
                .descriptionDuration(this.durations.description)
                .startingDuration(this.durations.starting)
                .gameDuration(this.durations.gameDuration)
                .gameOverDuration(this.durations.gameOver)
                .materialScores(this.materialScores != null ? this.materialScores : Collections.emptyMap())
                .materialBook(createMaterialBook())
                .recipes(this.recipes != null ? RecipeDTO.toRecipes(this.recipes) : Collections.emptyList())
                .recipeKeys(this.recipes != null ? RecipeDTO.toNamespacedKeys(this.recipes) : Collections.emptyList())
                .cropGrowerSpec(newCropGrowerSpec)
                .build();
    }
    
    private ItemStack createMaterialBook() {
        if (doNotGiveBookDebug) {
            return null;
        }
        ItemStack materialBook = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta.BookMetaBuilder builder = ((BookMeta) materialBook.getItemMeta()).toBuilder();
        BookMeta bookMeta = builder
                .title(Component.text("Item Values"))
                .author(Component.text("Farm Rush"))
                .pages(createPages())
                .build();
        materialBook.setItemMeta(bookMeta);
        return materialBook;
    }
    
    private List<Component> createPages() {
        List<TextComponent> lines = createLines();
        if (lines.isEmpty()) {
            return Collections.emptyList();
        }
        List<Component> pages = new ArrayList<>(lines.size()/15);
        for (int i = 0; i < lines.size(); i += 10) {
            TextComponent.Builder builder = Component.text();
            int end = Math.min(lines.size(), i + 10);
            
            for (int j = i; j < end; j++) {
                TextComponent line = lines.get(j);
                double length = PlainTextComponentSerializer.plainText().serialize(line).length();
                int numberOfExtraLines = (int) Math.ceil(length / 21.0) - 1;
                j += numberOfExtraLines;
                builder.append(line);
                if (j < end - 1) {
                    builder.append(Component.newline());
                }
            }
            pages.add(builder.build());
        }
        
        return pages;
    }
    
    private @NotNull List<TextComponent> createLines() {
        if (materialScores == null) {
            return Collections.emptyList();
        }
        List<TextComponent> lines = new ArrayList<>();
        List<Map.Entry<Material, ItemSale>> entryList = materialScores.entrySet().stream().sorted((entry1, entry2) -> {
            int score1 = entry1.getValue().getScore();
            int score2 = entry2.getValue().getScore();
            if (score1 != score2) {
                return Integer.compare(score2, score1);
            }
            int requiredAmount1 = entry1.getValue().getRequiredAmount();
            int requiredAmount2 = entry2.getValue().getRequiredAmount();
            if (requiredAmount1 != requiredAmount2) {
                return Integer.compare(requiredAmount1, requiredAmount2);
            }
            return entry1.getKey().compareTo(entry2.getKey());
        }).toList();
        
        for (Map.Entry<Material, ItemSale> entry : entryList) {
            Material material = entry.getKey();
            ItemSale itemSale = entry.getValue();
            Component itemName = Component.translatable(material.translationKey());
            TextComponent.Builder line = Component.text();
            if (itemSale.getRequiredAmount() > 1) {
                line
                        .append(Component.text(itemSale.getRequiredAmount()))
                        .append(Component.space());
            }
            line
                    .append(itemName)
                    .append(Component.text(": "))
                    .append(Component.text(itemSale.getScore())
                            .color(NamedTextColor.GOLD));
            lines.add(line.build());
        }
        return lines;
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
