package org.braekpo1nt.mctmanager.games.colossalcombat.config;

import com.google.common.base.Preconditions;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.ItemStackDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.util.BoundingBoxDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.LocationDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.PlayerInventoryDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @param version
 * @param world
 * @param spectatorArea
 * @param firstPlaceSpawn
 * @param secondPlaceSpawn
 * @param spectatorSpawn
 * @param requiredWins
 * @param itemDrops A list of location and item pairs. At the beginning of each round, these items will be spawned 
 *                  at their respective locations. Can be null or empty. Defaults to empty.
 * @param loadout
 * @param firstPlaceGate
 * @param secondPlaceGate
 * @param removeArea the area to remove all items from in between rounds
 * @param firstPlaceSupport
 * @param secondPlaceSupport
 * @param durations
 * @param description
 */
record ColossalCombatConfigDTO(
        String version, 
        String world, 
        BoundingBoxDTO spectatorArea, 
        LocationDTO firstPlaceSpawn, 
        LocationDTO secondPlaceSpawn, 
        LocationDTO spectatorSpawn, 
        int requiredWins, 
        List<ItemDrop> itemDrops,
        @Nullable PlayerInventoryDTO loadout, 
        Gate firstPlaceGate, 
        Gate secondPlaceGate, 
        BoundingBoxDTO removeArea, 
        BoundingBoxDTO firstPlaceSupport, 
        BoundingBoxDTO secondPlaceSupport, 
        Durations durations, 
        Component description) implements Validatable {
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(this.version, "version");
        validator.validate(Main.VALID_CONFIG_VERSIONS.contains(this.version), "invalid config version (%s)", this.version);
        validator.notNull(Bukkit.getWorld(this.world), "Could not find world \"%s\"", this.world);
    
        validator.notNull(this.spectatorArea, "spectatorArea");
        BoundingBox spectatorArea = this.spectatorArea.toBoundingBox();
        validator.validate(spectatorArea.getVolume() >= 1.0, "spectatorArea (%s) volume (%s) must be at least 1.0", spectatorArea, spectatorArea.getVolume());
    
        validator.notNull(this.firstPlaceSpawn, "firstPlaceSpawn");
        validator.notNull(this.secondPlaceSpawn, "secondPlaceSpawn");
        validator.notNull(this.spectatorSpawn, "spectatorSpawn");
        validator.validate(this.requiredWins > 0, "requiredWins must be greater than 0");
        
        if (this.itemDrops != null) {
            validator.validateList(this.itemDrops, "itemDrops");
        }
        
        if (this.loadout != null) {
            this.loadout.validate(validator.path("loadout"));
        }
    
        validator.notNull(this.firstPlaceGate, "firstPlaceGate");
        validator.notNull(this.firstPlaceGate.clearArea(), "firstPlaceGate.clearArea");
        validator.notNull(this.firstPlaceGate.placeArea(), "firstPlaceGate.placeArea");
        validator.notNull(this.firstPlaceGate.stone(), "firstPlaceGate.stone");
        validator.notNull(this.firstPlaceGate.antiSuffocationArea(), "firstPlaceGate.antiSuffocationArea");
        validator.validate(this.firstPlaceGate.antiSuffocationArea().toBoundingBox().getVolume() != 0.0, "firstPlaceGate.antiSuffocationArea volume can't be 0.0");
    
        validator.notNull(this.secondPlaceGate, "secondPlaceGate");
        validator.notNull(this.secondPlaceGate.clearArea(), "secondPlaceGate.clearArea");
        validator.notNull(this.secondPlaceGate.placeArea(), "secondPlaceGate.placeArea");
        validator.notNull(this.secondPlaceGate.stone(), "secondPlaceGate.stone");
        validator.notNull(this.secondPlaceGate.antiSuffocationArea(), "secondPlaceGate.antiSuffocationArea");
        validator.validate(this.secondPlaceGate.antiSuffocationArea().toBoundingBox().getVolume() != 0.0, "secondPlaceGate.antiSuffocationArea volume can't be 0.0");
    
        validator.notNull(this.removeArea, "removeArea");
        BoundingBox removeArea = this.removeArea.toBoundingBox();
        validator.validate(removeArea.getVolume() >= 2.0, "boundingBox (%s) volume (%s) must be at least 2.0", removeArea, removeArea.getVolume());
    
        validator.notNull(this.firstPlaceSupport, "firstPlaceSupport");
        BoundingBox firstPlaceSupport = this.firstPlaceSupport.toBoundingBox();
        validator.validate(firstPlaceSupport.getVolume() > 0, "firstPlaceSupport volume (%s) must be greater than 0", firstPlaceSupport.getVolume());
        validator.notNull(this.secondPlaceSupport, "secondPlaceSupport");
        BoundingBox secondPlaceSupport = this.secondPlaceSupport.toBoundingBox();
        validator.validate(secondPlaceSupport.getVolume() > 0, "secondPlaceSupport volume (%s) must be greater than 0", secondPlaceSupport.getVolume());
        validator.validate(!firstPlaceSupport.overlaps(secondPlaceSupport), "firstPlaceSupport and secondPlaceSupport can't overlap");
    
        validator.notNull(this.durations, "durations");
        validator.validate(this.durations.roundStarting() >= 1, "durations.roundStarting must be at least 1");
        validator.validate(this.durations.antiSuffocation() >= 0, "durations.antiSuffocation can't be negative");
        validator.notNull(this.description, "description");
    }
    
    public ColossalCombatConfig toConfig() {
        World newWorld = Bukkit.getWorld(this.world);
        Preconditions.checkState(newWorld != null, "Could not find world \"%s\"", this.world);
        
        List<Location> newItemDropLocations = new ArrayList<>();
        List<ItemStack> newItemDrops = new ArrayList<>();
        List<Boolean> newGlowingItemDrops = new ArrayList<>();
        if (this.itemDrops != null) {
            for (ItemDrop itemDrop : this.itemDrops) {
                newItemDropLocations.add(itemDrop.getLocation().toLocation(newWorld));
                newItemDrops.add(itemDrop.getItem().toItemStack());
                newGlowingItemDrops.add(itemDrop.isGlowing());
            }
        }
        
        return ColossalCombatConfig.builder()
                .world(newWorld)
                .firstPlaceSpawn(this.firstPlaceSpawn.toLocation(newWorld))
                .secondPlaceSpawn(this.secondPlaceSpawn.toLocation(newWorld))
                .spectatorSpawn(this.spectatorSpawn.toLocation(newWorld))
                .requiredWins(this.requiredWins)
                .itemDropLocations(newItemDropLocations)
                .itemDrops(newItemDrops)
                .glowingItemDrops(newGlowingItemDrops)
                .loadout(this.loadout != null ? this.loadout.toInventoryContents() : getDefaultLoadout())
                .firstPlaceClearArea(this.firstPlaceGate.clearArea.toBoundingBox())
                .firstPlacePlaceArea(this.firstPlaceGate.placeArea.toBoundingBox())
                .firstPlaceStone(this.firstPlaceGate.stone.toBoundingBox())
                .firstPlaceAntiSuffocationArea(this.firstPlaceGate.antiSuffocationArea.toBoundingBox())
                .secondPlaceClearArea(this.secondPlaceGate.clearArea.toBoundingBox())
                .secondPlacePlaceArea(this.secondPlaceGate.placeArea.toBoundingBox())
                .secondPlaceStone(this.secondPlaceGate.stone.toBoundingBox())
                .secondPlaceAntiSuffocationArea(this.secondPlaceGate.antiSuffocationArea.toBoundingBox())
                .removeArea(this.removeArea.toBoundingBox())
                .firstPlaceSupport(this.firstPlaceSupport.toBoundingBox())
                .secondPlaceSupport(this.secondPlaceSupport.toBoundingBox())
                .antiSuffocationDuration(this.durations.antiSuffocation)
                .roundStartingDuration(this.durations.roundStarting)
                .descriptionDuration(this.durations.description)
                .description(this.description)
                .build();
    }
    
    /**
     * @return the default loadout of the participants at the start of the round
     */
    static ItemStack[] getDefaultLoadout() {
        ItemStack[] result = new ItemStack[41];
        result[0] = new ItemStack(Material.STONE_SWORD);
        result[1] = new ItemStack(Material.BOW);
        result[2] = new ItemStack(Material.ARROW, 16);
        result[3] = new ItemStack(Material.COOKED_BEEF, 16);
        result[36] = new ItemStack(Material.LEATHER_BOOTS);
        result[38] = new ItemStack(Material.LEATHER_CHESTPLATE);
        return result;
    }
    
    record Gate(BoundingBoxDTO clearArea, BoundingBoxDTO placeArea, BoundingBoxDTO stone, BoundingBoxDTO antiSuffocationArea) {
    }
    
    @Data
    static class ItemDrop implements Validatable {
        private LocationDTO location;
        private ItemStackDTO item;
        private boolean glowing = false;
        
        @Override
        public void validate(@NotNull Validator validator) {
            validator.notNull(location, "location");
            validator.notNull(item, "item");
            item.validate(validator.path("item"));
        }
    }
    
    /**
     * @param roundStarting the duration (in seconds) to count down before the gates drop and the match starts
     * @param antiSuffocation the duration (in ticks) to prevent players from walking over the area that would cause them to suffocate in the concrete powder wall as the blocks fall. Careful, if this is not long enough the players will suffocate, and if it's too long they'll get frustrated. TODO: implement a more automated version of this. 
     */
    record Durations(int roundStarting, long antiSuffocation, int description) {
    }
}
