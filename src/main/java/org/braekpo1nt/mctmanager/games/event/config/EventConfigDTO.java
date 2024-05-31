package org.braekpo1nt.mctmanager.games.event.config;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.jetbrains.annotations.NotNull;

/**
 * @param title the title of the event, used in the sidebar and for announcing the winner
 * @param multipliers must have at least one element. The nth multiplier is used on the nth game in the event. If there are x multipliers, and we're on game z where z is greater than x, the xth multiplier is used. A multiplier will be multiplied by all points awarded during it's paired game.
 * @param durations various durations during the event
 */
record EventConfigDTO(
        String version, 
        String title, 
        double[] multipliers, 
        Durations durations) implements Validatable {
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(this.version, "version");
        validator.validate(Main.VALID_CONFIG_VERSIONS.contains(this.version), "invalid config version (%s)", this.version);
        validator.notNull(this.title, "title");
        validator.validate(this.title.length() >= 1, "title must be at least 1 character");
        validator.notNull(this.multipliers, "multipliers");
        validator.validate(this.multipliers.length >= 1, "there must be at least 1 multiplier");
        validator.notNull(this.durations, "durations");
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
                .title(this.title)
                .build();
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
