package org.braekpo1nt.mctmanager.games.game.parkourpathway;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.braekpo1nt.mctmanager.utils.BlockPlacementUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class TeamSpawn {
    private final World world;
    /**
     * the area to for the barrier blocks to be placed/removed
     * @see TeamSpawn#close() 
     * @see TeamSpawn#open() 
     */
    private final BoundingBox barrierArea;
    /**
     * the location the team should be teleported to
     */
    private final Location spawnLocation;
    /**
     * The material to create the barrier out of
     */
    private Material barrierMaterial = Material.GLASS;
    
    /**
     * actually constructs the {@link TeamSpawn} in the given world with the given material. Replaces {@link Material#AIR} with the assigned {@link TeamSpawn#barrierMaterial}
     */
    public void close() {
        BlockPlacementUtils.createCubeReplace(world, barrierArea, Material.AIR, barrierMaterial);
    }
    
    /**
     * opens the {@link TeamSpawn}
     */
    public void open() {
        BlockPlacementUtils.createCubeReplace(world, barrierArea, barrierMaterial, Material.AIR);
    }
    
    public void teleport(Player participant) {
        participant.teleport(spawnLocation);
    }
}
