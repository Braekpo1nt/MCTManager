package org.braekpo1nt.mctmanager.games.game.clockwork.config;

import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.clockwork.Chaos;
import org.braekpo1nt.mctmanager.games.game.clockwork.Wedge;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@Builder
public class ClockworkConfig {
    
    private World world;
    private Location startingLocation;
    private int rounds;
    private int playerEliminationScore;
    private int teamEliminationScore;
    private int winRoundScore;
    private int breatherDuration;
    private int getToWedgeDuration;
    private int stayOnWedgeDuration;
    private double initialChimeInterval;
    private double chimeIntervalDecrement;
    private String clockChimeSound;
    private float clockChimeVolume;
    private float clockChimePitch;
    public Chaos chaos;
    public Team.OptionStatus collisionRule;
    private List<Wedge> wedges;
    private List<Material> preventInteractions;
    private int descriptionDuration;
    private @Nullable BoundingBox spectatorArea;
    private Component description;
    
}
