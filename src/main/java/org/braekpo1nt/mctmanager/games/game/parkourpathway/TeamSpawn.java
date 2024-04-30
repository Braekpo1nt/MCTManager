package org.braekpo1nt.mctmanager.games.game.parkourpathway;

import lombok.AllArgsConstructor;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

@AllArgsConstructor
public class TeamSpawn {
    private final World world;
    private final BoundingBox barrierArea;
    private final Location spawnLocation;
    
    /**
     * actually constructs the {@link TeamSpawn} in the given world with the given material
     * @param barrierMaterial the material to create the barrier out of
     */
    public void close(Material barrierMaterial) {
        BlockPlacementUtils.createCube(world, barrierArea, barrierMaterial);
    }
    
    /**
     * opens the {@link TeamSpawn}
     */
    public void open() {
        BlockPlacementUtils.createCube(world, barrierArea, Material.AIR);
    }
    
    public void teleport(Player participant) {
        participant.teleport(spawnLocation);
    }
}
