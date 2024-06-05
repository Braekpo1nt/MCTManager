package org.braekpo1nt.mctmanager.games.colossalcombat.config;

import com.google.common.base.Preconditions;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.util.BoundingBoxDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.LocationDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.PlayerInventoryDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 
 * @param version
 * @param world
 * @param spectatorArea
 * @param firstPlaceSpawn
 * @param secondPlaceSpawn
 * @param spectatorSpawn
 * @param requiredWins
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
        @Nullable PlayerInventoryDTO loadout, 
        Gate firstPlaceGate, 
        Gate secondPlaceGate, 
        BoundingBoxDTO removeArea, 
        BoundingBoxDTO firstPlaceSupport, 
        BoundingBoxDTO secondPlaceSupport, 
        CaptureTheFlag captureTheFlag,
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
        
        if (captureTheFlag != null) {
            captureTheFlag.validate(validator.path("captureTheFlag"));
        }
    
        validator.notNull(this.durations, "durations");
        validator.validate(this.durations.roundStarting() >= 1, "durations.roundStarting must be at least 1");
        validator.validate(this.durations.antiSuffocation() >= 0, "durations.antiSuffocation can't be negative");
        validator.notNull(this.description, "description");
    }
    
    public ColossalCombatConfig toConfig() {
        World newWorld = Bukkit.getWorld(this.world);
        Preconditions.checkState(newWorld != null, "Could not find world \"%s\"", this.world);
        
        ColossalCombatConfig.ColossalCombatConfigBuilder builder = ColossalCombatConfig.builder()
                .world(newWorld)
                .firstPlaceSpawn(this.firstPlaceSpawn.toLocation(newWorld))
                .secondPlaceSpawn(this.secondPlaceSpawn.toLocation(newWorld))
                .spectatorSpawn(this.spectatorSpawn.toLocation(newWorld))
                .requiredWins(this.requiredWins)
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
                .description(this.description);
        if (captureTheFlag != null) {
            builder.shouldStartCaptureTheFlag(true)
                    .firstPlaceFlagGoal(this.captureTheFlag.firstPlaceGoal.toBoundingBox())
                    .secondPlaceFlagGoal(this.captureTheFlag.secondPlaceGoal.toBoundingBox())
                    .flagMaterial(this.captureTheFlag.flagMaterial)
                    .initialFlagDirection(this.captureTheFlag.flagDirection)
                    .flagLocation(this.captureTheFlag.flagLocation.toLocation(newWorld))
                    .flagSpawnMessage(this.captureTheFlag.flagSpawnMessage)
                    .captureTheFlagMaximumPlayers(this.captureTheFlag.maxPlayers)
                    .captureTheFlagDuration(this.captureTheFlag.countdown)
                    .replaceBlock(this.captureTheFlag.replaceBlock)
                    .firstPlaceFlagReplaceArea(this.captureTheFlag.firstPlaceReplaceArea.toBoundingBox())
                    .secondPlaceFlagReplaceArea(this.captureTheFlag.secondPlaceReplaceArea.toBoundingBox());
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
        private BoundingBoxDTO firstPlaceGoal;
        private BoundingBoxDTO secondPlaceGoal;
        /**
         * the block to replace with concrete of the team's color. Defaults to null. If null, no blocks will be replaced.
         */
        private @Nullable Material replaceBlock = null;
        /**
         * The area to replace with the concrete of the team's color. At the start of the game, the {@link CaptureTheFlag#replaceBlock} material will be replaced with the concrete of the team's color, and at the end of the game it will be returned to what it was before.
         */
        private BoundingBoxDTO firstPlaceReplaceArea;
        /**
         * The same as {@link CaptureTheFlag#firstPlaceReplaceArea}, but for the second place team.
         */
        private BoundingBoxDTO secondPlaceReplaceArea;
        
        @Override
        public void validate(@NotNull Validator validator) {
            validator.notNull(flagMaterial, "flagMaterial");
            validator.notNull(flagDirection, "flagDirection");
            validator.notNull(flagLocation, "flagLocation");
            validator.notNull(flagSpawnMessage, "flagSpawnMessage");
            validator.notNull(firstPlaceGoal, "firstPlaceGoal");
            validator.validate(firstPlaceGoal.toBoundingBox().getVolume() >= 1, "firstPlaceGoal must have a volume of at least 1");
            validator.notNull(secondPlaceGoal, "secondPlaceGoal");
            validator.validate(secondPlaceGoal.toBoundingBox().getVolume() >= 1, "secondPlaceGoal must have a volume of at least 1");
            validator.notNull(firstPlaceReplaceArea, "firstPlaceReplaceArea");
            validator.notNull(secondPlaceReplaceArea, "secondPlaceReplaceArea");
        }
    }
    
    record Gate(BoundingBoxDTO clearArea, BoundingBoxDTO placeArea, BoundingBoxDTO stone, BoundingBoxDTO antiSuffocationArea) {
    }
    
    /**
     * @param roundStarting the duration (in seconds) to count down before the gates drop and the match starts
     * @param antiSuffocation the duration (in ticks) to prevent players from walking over the area that would cause them to suffocate in the concrete powder wall as the blocks fall. Careful, if this is not long enough the players will suffocate, and if it's too long they'll get frustrated. TODO: implement a more automated version of this. 
     */
    record Durations(int roundStarting, long antiSuffocation, int description) {
    }
}
