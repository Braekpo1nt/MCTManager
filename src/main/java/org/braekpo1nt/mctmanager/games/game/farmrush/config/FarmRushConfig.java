package org.braekpo1nt.mctmanager.games.game.farmrush.config;

import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;

@Data
@Builder
public class FarmRushConfig {
    private Location adminLocation;
    private Component description;
    private int descriptionDuration;
}
