package org.braekpo1nt.mctmanager.games.event.config;

import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Data
@Builder
public class EventConfig {
    
    private int waitingInHubDuration;
    private int halftimeBreakDuration;
    private int votingDuration;
    private int startingGameDuration;
    private int backToHubDuration;
    private double[] multipliers;
    private boolean shouldDisplayGameNumber;
    /**
     * Contains which configs to use for which games. 
     * Can't be null, but can be empty. If a config doesn't exist for a given
     * game, the default will be used.
     */
    private @NotNull Map<GameType, String> gameConfigs;
    private Component title;
    
    public boolean shouldDisplayGameNumber() {
        return shouldDisplayGameNumber;
    }
}
