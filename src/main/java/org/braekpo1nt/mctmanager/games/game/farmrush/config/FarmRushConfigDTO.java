package org.braekpo1nt.mctmanager.games.game.farmrush.config;

import com.google.common.base.Preconditions;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.LocationDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.ChestInventoryDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.PlayerInventoryDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
public class FarmRushConfigDTO implements Validatable {
    
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
    private Component description;
    private Durations durations;
    private @Nullable Map<Material, Integer> materialScores;
    
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
        // TODO: implement this
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
    private static void addScoreLore(@Nullable ItemStack item, @NotNull Map<Material, Integer> materialScores) {
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
    private static @Nullable Component getScoreLore(@Nullable Material type, Map<Material, Integer> materialScores) {
        if (type == null) {
            return null;
        }
        Integer score = materialScores.get(type);
        if (score == null) {
            return null;
        }
        return Component.empty()
                .append(Component.text("Price: "))
                .append(Component.text(score))
                .color(NamedTextColor.GOLD);
    }
}
