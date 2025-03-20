package org.braekpo1nt.mctmanager.games.event.config;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.text.Component;

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
    private List<Tip> tips;
    private int tipsDisplayTimeSeconds;
    private Component title;

    public boolean shouldDisplayGameNumber() {
        return shouldDisplayGameNumber;
    }
}
