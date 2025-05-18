package org.braekpo1nt.mctmanager.games.game.example.config;

import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.List;

@Data
@Builder
public class ExampleConfig {
    private World world;
    private Location startingLocation;
    private Component description;
    private SpectatorBoundary spectatorBoundary;
    private int jumpScore;
    private List<Material> preventInteractions;
}
