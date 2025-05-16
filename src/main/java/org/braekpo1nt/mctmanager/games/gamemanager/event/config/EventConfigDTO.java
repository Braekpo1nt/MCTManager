package org.braekpo1nt.mctmanager.games.gamemanager.event.config;

import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.gamemanager.event.Tip;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetDTO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

import java.util.List;

/**
 * @param title the title of the event, used in the sidebar and for announcing the winner
 * @param multipliers must have at least one element. The nth multiplier is used on the nth game in the event. If there are x multipliers, and we're on game z where z is greater than x, the xth multiplier is used. A multiplier will be multiplied by all points awarded during it's paired game.
 * @param durations various durations during the event
 */
record EventConfigDTO(
        String version, 
        Component title, 
        double[] multipliers, 
        boolean shouldDisplayGameNumber,
        @Nullable Map<GameType, String> gameConfigs,
        @Nullable String colossalCombatConfig,
        Tips tips,
        PresetDTO.PresetConfigDTO preset,
        Durations durations) implements Validatable {
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(this.version, "version");
        validator.validate(Main.VALID_CONFIG_VERSIONS.contains(this.version), "invalid config version (%s)", this.version);
        validator.notNull(this.title, "title");
        validator.notNull(this.multipliers, "multipliers");
        validator.validate(this.multipliers.length >= 1, "there must be at least 1 multiplier");
        if (gameConfigs != null) {
            validator.validate(!gameConfigs.containsValue(null), "gameConfigs can't contain null values");
        }
        validator.notNull(this.durations, "durations");
        validator.notNull(this.tips, "tips");
        tips.validate(validator.path("tips"));
        validator.notNull(this.tips.displayTime, "display time for tips must be specified");
        validator.validate(!this.tips.getTips().isEmpty(), "there must be at least 1 game tip");
        if (preset != null) {
            preset.validate(validator.path("preset"));
        }
        validator.validate(this.durations.waitingInHub() >= 0, "durations.waitingInHub can't be negative");
        validator.validate(this.durations.halftimeBreak() >= 0, "durations.halftimeBreak can't be negative");
        validator.validate(this.durations.voting() >= 0, "durations.voting can't be negative");
        validator.validate(this.durations.startingGame() >= 0, "durations.startingGame can't be negative");
        validator.validate(this.durations.backToHub() >= 0, "durations.backToHub can't be negative");
    }
    
    EventConfig toConfig() {
        return EventConfig.builder()
                .waitingInHubDuration(this.durations.waitingInHub)
                .halftimeBreakDuration(this.durations.halftimeBreak)
                .votingDuration(this.durations.voting)
                .startingGameDuration(this.durations.startingGame)
                .backToHubDuration(this.durations.backToHub)
                .multipliers(this.multipliers)
                .tips(TipDTO.toTips(this.tips.getTips()))
                .tipsDisplayTime(this.tips.getDisplayTime())
                .shouldDisplayGameNumber(this.shouldDisplayGameNumber)
                .gameConfigs(this.gameConfigs != null ? this.gameConfigs : Collections.emptyMap())
                .colossalCombatConfig(this.colossalCombatConfig != null ? this.colossalCombatConfig : "default.json")
                .preset(this.preset != null ? this.preset.toPreset() : null)
                .title(this.title)
                .build();
    }

    @Data
    static class Tips implements Validatable {
        /**
         * how long to display each tip in ticks
         */
        private int displayTime;
        /**
         * the tips, consisting of text and a priority
         */
        private List<TipDTO> tips;
        
        @Override
        public void validate(@NotNull Validator validator) {
            validator.validate(displayTime > 0, "displayTime must be greater than 0");
            validator.validateList(tips, "tips");
            
        }
    }
    
    @Data
    static class TipDTO implements Validatable {
        /**
         * The body of the tip
         */
        private Component body;
        /**
         * the priority of the tip (higher means more often)
         */
        private int priority;
        
        @Override
        public void validate(@NotNull Validator validator) {
            validator.notNull(body, "body");
            validator.validate(priority >= 1, "priority must be at least 1");
        }
        
        Tip toTip() {
            return new Tip(this.priority, this.body);
        }
        
        /**
         * Converts the Tip records to a List of Tip objects
         */
        static List<Tip> toTips(List<TipDTO> dtos) {
            return dtos.stream()
                    .map(TipDTO::toTip)
                    .toList();
        }
    }
    
    /**
     * All units are seconds, none can be negative.
     * @param waitingInHub the time spent waiting in the hub between games (seconds)
     * @param halftimeBreak the duration of the halftime break (seconds)
     * @param voting the duration of the voting phase (seconds)
     * @param startingGame the delay to start the game after the voting phase (seconds)
     * @param backToHub the delay after a game is over before returning to the hub (seconds)
     */
    record Durations(int waitingInHub, int halftimeBreak, int voting, int startingGame, int backToHub) {
    }
    
}
