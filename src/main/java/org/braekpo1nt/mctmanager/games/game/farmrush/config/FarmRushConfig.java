package org.braekpo1nt.mctmanager.games.game.farmrush.config;

import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.farmrush.Arena;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
@Builder
public class FarmRushConfig {
    private World world;
    private Location adminLocation;
    private Component description;
    private int descriptionDuration;
    /**
     * The first arena placement
     */
    private Arena firstArena;
    /**
     * The file location of the arena, relative to the Plugin's data folder
     */
    private String arenaFile;
    private @NotNull List<Material> preventInteractions;
}
