package org.braekpo1nt.mctmanager.games.event.config;

import com.google.gson.JsonParseException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.annotations.JsonAdapter;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

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
        TipsConfig tips,
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
        validator.notNull(this.tips.displayTimeSeconds, "display time for tips must be specified");
        validator.validate(!this.tips.tips().isEmpty(), "there must be at least 1 game tip");
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
                .tips(convertTips())
                .tipsDisplayTimeSeconds(this.tips.displayTimeSeconds())
                .shouldDisplayGameNumber(this.shouldDisplayGameNumber)
                .gameConfigs(this.gameConfigs != null ? this.gameConfigs : Collections.emptyMap())
                .colossalCombatConfig(this.colossalCombatConfig != null ? this.colossalCombatConfig : "default.json")
                .title(this.title)
                .build();
    }

    /**
     * Converts the Tip records to a List of Tip objects
     */
    private List<Tip> convertTips() {
        return this.tips.tips().stream()
                .map(tipDto -> new Tip(
                        tipDto.priority(),
                        tipDto.text()))
                .collect(Collectors.toList());
    }

    /**
     * Record to hold all configured tips and the tip display time
     *
     * @param displayTimeSeconds the amount of seconds to display a tip
     * @param tips               the tips, consisting of text and a priority
     */
    record TipsConfig(int displayTimeSeconds, List<TipDTO> tips) {
    }

    /**
     * Record to hold the information contained in a @Tip object
     *
     * @param text     the tip text as a component
     * @param priority the tip priority
     */
    record TipDTO(@JsonAdapter(ComponentDeserializer.class) Component text, int priority) {
    }

    // Helper class to deserialize the "text" field of a Tip in the json config into a Component
    static class ComponentDeserializer implements JsonDeserializer<Component> {
        @Override
        public Component deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return GsonComponentSerializer.gson().deserializeFromTree(json);
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
