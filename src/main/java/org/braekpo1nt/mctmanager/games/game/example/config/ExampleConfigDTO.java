package org.braekpo1nt.mctmanager.games.game.example.config;

import com.google.common.base.Preconditions;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.LocationDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@Data
public class ExampleConfigDTO implements Validatable {
    
    private String world;
    private LocationDTO startingLocation;
    private BoundingBox spectatorArea;
    private @Nullable List<Material> preventInteractions;
    private Component description;
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(this.world, "world");
        validator.notNull(Bukkit.getWorld(this.world), "Could not find world \"%s\"", this.world);
        validator.notNull(this.startingLocation, "startingLocation");
        validator.notNull(this.spectatorArea, "spectatorArea");
        validator.notNull(this.description, "description");
    }
    
    public ExampleConfig toConfig() {
        World newWorld = Bukkit.getWorld(this.world);
        Preconditions.checkState(newWorld != null, "Could not find world \"%s\"", this.world);
        
        Location locationLocation = this.startingLocation.toLocation(newWorld);
        return ExampleConfig.builder()
                .world(newWorld)
                .startingLocation(locationLocation)
                .description(this.description)
                .spectatorBoundary(new SpectatorBoundary(this.spectatorArea, locationLocation))
                .preventInteractions(this.preventInteractions != null ? this.preventInteractions : Collections.emptyList())
                .build();
    }
}
