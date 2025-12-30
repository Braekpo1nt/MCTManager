package org.braekpo1nt.mctmanager.games.game.finalgame.config;

import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.LocationDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.inventory.PlayerInventoryDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalGameKit;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class FinalGameKitDTO implements Validatable {
    /**
     * The display name of the kit
     */
    private Component name;
    private Material menuItemMaterial;
    private List<Component> menuItemLore;
    private @Nullable Integer copies;
    private List<LocationDTO> northSpawns;
    private List<LocationDTO> southSpawns;
    private @Nullable Boolean melee;
    private PlayerInventoryDTO loadout;
    private List<RefillDTO> refills;
    private @Nullable Integer refillSeconds;
    private @Nullable Boolean hasBanner;
    
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(name, "name");
        validator.notNull(menuItemMaterial, "menuItemMaterial");
        validator.notNull(menuItemLore, "menuItemLore");
        validator.validate(!menuItemLore.contains(null), "menuItemLore can't contain null entries");
        validator.notNull(menuItemLore, "menuItemLore");
        validator.notEmpty(northSpawns, "northSpawns");
        validator.validate(!northSpawns.contains(null), "northSpawns can't contain null entries");
        validator.notEmpty(southSpawns, "southSpawns");
        validator.validate(!southSpawns.contains(null), "southSpawns can't contain null entries");
        if (copies != null) {
            validator.validate(copies >= 1, "copies must be greater than or equal to 1");
            // this is a provision for if Stg. Shotgun wants this. The actual feature allows for fewer spawns as copies, because it loops through them.
            validator.validate(copies == northSpawns.size(), "northSpawns must have the same number of entries as there are copies for this kit (%s)", copies);
            validator.validate(copies == southSpawns.size(), "southSpawns must have the same number of entries as there are copies for this kit (%s)", copies);
        }
        validator.notNull(loadout, "loadout");
        loadout.validate(validator.path("loadout"));
        validator.notNull(refills, "refills");
        validator.validateList(refills, "refills");
    }
    
    public static Map<String, FinalGameKit> toFinalGameKits(@NotNull Map<String, FinalGameKitDTO> dtos, @NotNull World world) {
        return dtos.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().toFinalGameKit(world)
                ));
    }
    
    /**
     * @param world the world that all locations are in
     * @return the translated kit
     */
    public FinalGameKit toFinalGameKit(@NotNull World world) {
        return FinalGameKit.builder()
                .displayName(name)
                .menuItemMaterial(menuItemMaterial)
                .menuItemLore(menuItemLore)
                .copies(copies != null ? copies : 1)
                .northSpawns(LocationDTO.toLocations(northSpawns, world))
                .southSpawns(LocationDTO.toLocations(southSpawns, world))
                .melee(melee != null ? melee : true)
                .loadout(loadout.toInventoryContents())
                .refills(RefillDTO.toRefills(refills))
                .refillSeconds(refillSeconds != null ? refillSeconds : 20)
                .hasBanner(hasBanner != null ? hasBanner : false)
                .build();
    }
    
    
    @Data
    static class RefillDTO implements Validatable {
        private Material material;
        private int amount;
        private int max;
        
        @Override
        public void validate(@NotNull Validator validator) {
            validator.notNull(material, "material");
            validator.validate(amount >= 1, "amount must be greater than or equal to 1");
            validator.validate(max >= amount, "max must be greater than or equal to amount");
        }
        
        public FinalGameKit.Refill toRefill() {
            return FinalGameKit.Refill.builder()
                    .material(material)
                    .amount(amount)
                    .max(max)
                    .build();
        }
        
        public static List<FinalGameKit.Refill> toRefills(List<RefillDTO> dtos) {
            return dtos.stream().map(RefillDTO::toRefill).toList();
        }
    }
}
