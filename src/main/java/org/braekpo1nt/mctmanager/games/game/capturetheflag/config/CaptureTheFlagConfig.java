package org.braekpo1nt.mctmanager.games.game.capturetheflag.config;

import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.config.SpectatorBoundary;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.Arena;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.Loadout;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class CaptureTheFlagConfig {
    
    private World world;
    private Location spawnObservatory;
    private List<Arena> arenas;
    private Map<String, Loadout> loadouts;
    private int matchesStartingDuration;
    private int roundTimerDuration;
    private int classSelectionDuration;
    private int winScore;
    private int killScore;
    private int descriptionDuration;
    private int roundOverDuration;
    private int gameOverDuration;
    private List<Material> preventInteractions;
    private @Nullable SpectatorBoundary spectatorBoundary;
    private Component description;
    
}
