package org.braekpo1nt.mctmanager.games.gamemanager.event.config;

import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.gamemanager.event.Tip;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetConfig;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import java.util.List;

@Data
@Builder
public class EventConfig {
    
    private int waitingInHubDuration;
    private int halftimeBreakDuration;
    private int votingDuration;
    private int startingGameDuration;
    private int backToHubDuration;
    private double[] multipliers;
    private List<Tip> tips;
    /**
     * how long to display each tip in ticks
     */
    private int tipsDisplayTime;
    private boolean shouldDisplayGameNumber;
    /**
     * Contains which configs to use for which games. 
     * Can't be null, but can be empty. If a config doesn't exist for a given
     * game, the default will be used.
     */
    private @NotNull Map<GameType, String> gameConfigs;
    private @NotNull String colossalCombatConfig;
    /**
     * The preset that should be applied when switching to event mode
     * If null, no preset is applied.
     */
    private @Nullable PresetConfig preset;
    /**
     * The crown to give to players when they win the event
     */
    private ItemStack crown;
    private Component title;
    
}
