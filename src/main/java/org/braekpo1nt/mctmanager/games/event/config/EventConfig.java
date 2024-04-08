package org.braekpo1nt.mctmanager.games.event.config;

/**
 * @param title the title of the event, used in the sidebar and for announcing the winner
 * @param multipliers must have at least one element. The nth multiplier is used on the nth game in the event. If there are x multipliers, and we're on game z where z is greater than x, the xth multiplier is used. A multiplier will be multiplied by all points awarded during it's paired game.
 * @param durations various durations during the event
 */
record EventConfig(String version, String title, double[] multipliers, Durations durations) {
    
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
