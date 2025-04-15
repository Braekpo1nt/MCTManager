package org.braekpo1nt.mctmanager.games.game.colossalcombat.config;

import com.google.common.base.Preconditions;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.LocationDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.ItemStackDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.PlayerInventoryDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.Gate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

record ColossalCombatConfigDTO(
        String version, 
        String world, 
        @Nullable BoundingBox spectatorArea, 
        LocationDTO spectatorSpawn, 
        int requiredWins, 
        List<ItemDrop> itemDrops,
        @Nullable PlayerInventoryDTO loadout, 
        GateDTO northGate, 
        GateDTO southGate, 
        BoundingBox removeArea, 
        BoundingBox northSupport, 
        BoundingBox southSupport, 
        CaptureTheFlag captureTheFlag,
        @Nullable List<Material> preventInteractions,
        Durations durations, 
        Component description) implements Validatable {
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(this.version, "version");
        validator.validate(Main.VALID_CONFIG_VERSIONS.contains(this.version), "invalid config version (%s)", this.version);
        validator.notNull(Bukkit.getWorld(this.world), "Could not find world \"%s\"", this.world);
        
        if (spectatorArea != null) {
            BoundingBox spectatorArea = this.spectatorArea;
            validator.validate(spectatorArea.getVolume() >= 1.0, "spectatorArea (%s) volume (%s) must be at least 1.0", spectatorArea, spectatorArea.getVolume());
        }
    
        
        validator.notNull(this.spectatorSpawn, "spectatorSpawn");
        validator.validate(this.requiredWins > 0, "requiredWins must be greater than 0");
    
        if (this.itemDrops != null) {
            validator.validateList(this.itemDrops, "itemDrops");
        }
        
        if (this.loadout != null) {
            this.loadout.validate(validator.path("loadout"));
        }
    
        validator.notNull(this.northGate, "northGate");
        this.northGate.validate(validator.path("northGate"));
        validator.notNull(this.southGate, "southGate");
        this.southGate.validate(validator.path("southGate"));
    
        validator.notNull(this.removeArea, "removeArea");
        validator.validate(this.removeArea.getVolume() >= 2.0, "boundingBox (%s) volume (%s) must be at least 2.0", removeArea, removeArea.getVolume());
    
        validator.notNull(this.northSupport, "northSupport");
        validator.validate(this.northSupport.getVolume() > 0, "northSupport volume (%s) must be greater than 0", northSupport.getVolume());
        validator.notNull(this.southSupport, "southSupport");
        validator.validate(this.southSupport.getVolume() > 0, "southSupport volume (%s) must be greater than 0", southSupport.getVolume());
        validator.validate(!northSupport.overlaps(southSupport), "northSupport and southSupport can't overlap");
        
        validator.notNull(captureTheFlag, "captureTheFlag");
        captureTheFlag.validate(validator.path("captureTheFlag"));
    
        validator.notNull(this.durations, "durations");
        validator.validate(this.durations.roundStarting() >= 1, "durations.roundStarting must be at least 1");
        validator.validate(this.durations.roundOver() >= 0, "durations.roundOver can't be negative");
        validator.validate(this.durations.gameOver() >= 0, "durations.gameOver can't be negative");
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
        
        ColossalCombatConfig.ColossalCombatConfigBuilder builder = ColossalCombatConfig.builder()
                .world(newWorld)
                .spectatorSpawn(this.spectatorSpawn.toLocation(newWorld))
                .spectatorBoundary(this.spectatorArea == null ? null :
                        new SpectatorBoundary(this.spectatorArea, this.spectatorSpawn.toLocation(newWorld)))
                .requiredWins(this.requiredWins)
                .loadout(this.loadout != null ? this.loadout.toInventoryContents() : getDefaultLoadout())
                .removeArea(this.removeArea)
                .northGate(this.northGate.toGate(newWorld))
                .southGate(this.southGate.toGate(newWorld))
                .northSupport(this.northSupport)
                .southSupport(this.southSupport)
                .antiSuffocationDuration(this.durations.antiSuffocation)
                .roundStartingDuration(this.durations.roundStarting)
                .roundOverDuration(this.durations.roundOver)
                .gameOverDuration(this.durations.gameOver)
                .descriptionDuration(this.durations.description)
                .itemDrops(newItemDrops)
                .itemDropLocations(newItemDropLocations)
                .glowingItemDrops(newGlowingItemDrops)
                .preventInteractions(this.preventInteractions != null ? this.preventInteractions : Collections.emptyList())
                .description(this.description);
        
        if (captureTheFlag != null) {
            builder.shouldStartCaptureTheFlag(true)
                    .flagMaterial(this.captureTheFlag.flagMaterial)
                    .initialFlagDirection(this.captureTheFlag.flagDirection)
                    .flagLocation(this.captureTheFlag.flagLocation.toLocation(newWorld))
                    .flagSpawnMessage(this.captureTheFlag.flagSpawnMessage)
                    .captureTheFlagMaximumPlayers(this.captureTheFlag.maxPlayers)
                    .captureTheFlagDuration(this.captureTheFlag.countdown)
                    .replaceBlock(this.captureTheFlag.replaceBlock)
                    .northFlagReplaceArea(this.captureTheFlag.firstPlaceReplaceArea)
                    .southFlagReplaceArea(this.captureTheFlag.secondPlaceReplaceArea);
        } else {
            builder.shouldStartCaptureTheFlag(false);
        }
        return builder.build();
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
    
    @Data
    static class CaptureTheFlag implements Validatable {
        private Material flagMaterial = Material.WHITE_BANNER;
        private BlockFace flagDirection = BlockFace.NORTH;
        private LocationDTO flagLocation;
        private Component flagSpawnMessage = Component.text("The flag has appeared! Capture it to win the round!")
                .color(NamedTextColor.GREEN);
        /** 
         * the max number of players on each team for the capture the flag countdown to start. 
         * defaults to 1. Negative values will never be triggered.
         */
        private int maxPlayers = 1;
        /**
         * The countdown (in seconds) for when the flag will spawn once the maxPlayers limit has been reached.
         * Defaults to 60.
         */
        private int countdown = 60;
        private BoundingBox firstPlaceGoal;
        private BoundingBox secondPlaceGoal;
        /**
         * the block to replace with concrete of the team's color. Defaults to null. If null, no blocks will be replaced.
         */
        private @Nullable Material replaceBlock = null;
        /**
         * The area to replace with the concrete of the team's color. At the start of the game, the {@link CaptureTheFlag#replaceBlock} material will be replaced with the concrete of the team's color, and at the end of the game it will be returned to what it was before.
         */
        private BoundingBox firstPlaceReplaceArea;
        /**
         * The same as {@link CaptureTheFlag#firstPlaceReplaceArea}, but for the second place team.
         */
        private BoundingBox secondPlaceReplaceArea;
        
        @Override
        public void validate(@NotNull Validator validator) {
            validator.notNull(flagMaterial, "flagMaterial");
            validator.notNull(flagDirection, "flagDirection");
            validator.notNull(flagLocation, "flagLocation");
            validator.notNull(flagSpawnMessage, "flagSpawnMessage");
            validator.notNull(firstPlaceGoal, "firstPlaceGoal");
            validator.validate(firstPlaceGoal.getVolume() >= 1, "firstPlaceGoal must have a volume of at least 1");
            validator.notNull(secondPlaceGoal, "secondPlaceGoal");
            validator.validate(secondPlaceGoal.getVolume() >= 1, "secondPlaceGoal must have a volume of at least 1");
            validator.notNull(firstPlaceReplaceArea, "firstPlaceReplaceArea");
            validator.notNull(secondPlaceReplaceArea, "secondPlaceReplaceArea");
        }
    }
    
    @Data
    static class GateDTO implements Validatable {
        private LocationDTO spawn;
        private BoundingBox clearArea;
        private BoundingBox placeArea;
        private BoundingBox stone;
        private BoundingBox antiSuffocationArea;
        private BoundingBox flagGoal;
        
        public Gate toGate(World world) {
            return Gate.builder()
                    .spawn(this.spawn.toLocation(world))
                    .clearArea(this.clearArea)
                    .placeArea(this.placeArea)
                    .stone(this.stone)
                    .antiSuffocationArea(this.antiSuffocationArea)
                    .flagGoal(this.flagGoal)
                    .build();
        }
        
        @Override
        public void validate(@NotNull Validator validator) {
            validator.notNull(spawn, "spawn");
            validator.notNull(clearArea, "clearArea");
            validator.notNull(placeArea, "placeArea");
            validator.notNull(stone, "stone");
            validator.notNull(antiSuffocationArea, "antiSuffocationArea");
            validator.notNull(flagGoal, "flagGoal");
        }
    }
    
    /**
     * @param roundStarting the duration (in seconds) to count down before the gates drop and the match starts
     * @param antiSuffocation the duration (in ticks) to prevent players from walking over the area that would cause 
     *                        them to suffocate in the concrete powder wall as the blocks fall. Careful, if this is not 
     *                        long enough the players will suffocate, and if it's too long they'll get frustrated. 
     *                        TODO: implement a more automated version of this. 
     */
    record Durations(int roundStarting, int roundOver, int gameOver, long antiSuffocation, int description) {
    }
}
