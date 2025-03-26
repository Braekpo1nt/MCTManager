package org.braekpo1nt.mctmanager.games.game.example.config;

import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;

@Data
@Builder
public class ExampleConfig {
    private World world;
    private Location startingLocation;
    private Component description;
}
