package org.braekpo1nt.mctmanager.games.enums;

import org.jetbrains.annotations.NotNull;

public enum MCTGames {
    FOOT_RACE,
    MECHA,
    CAPTURE_THE_FLAG,
    SPLEEF,
    FINAL_GAME, 
    PARKOUR_PATHWAY, 
    CLOCKWORK, COLOSSAL_COLOSSEUM;
    
    public static @NotNull String getTitle(@NotNull MCTGames mctGame) {
        switch (mctGame) {
            case FOOT_RACE -> {
                return "Foot Race";
            }
            case MECHA -> {
                return "MECHA";
            }
            case CAPTURE_THE_FLAG -> {
                return "Capture the Flag";
            }
            case SPLEEF -> {
                return "Spleef";
            }
            case PARKOUR_PATHWAY -> {
                return "Parkour Pathway";
            }
            case CLOCKWORK -> {
                return "Clockwork";
            }
            case FINAL_GAME -> {
                return "Final Game";
            }
            default -> {
                return "";
            }
        }
    }
}
