package org.braekpo1nt.mctmanager.games.gamestate.preset;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PresetOpts {
    
    public static PresetOpts allFalse() {
        return PresetOpts.builder()
                .build();
    }
    
    /**
     * if true, all previous teams and participants will be cleared and the preset
     * teams and participants will be added (thus replacing everything with the
     * preset). If false, the previous GameSate will not be changed, and it will
     * try to add all teams from the preset but not override existing teams,
     * and participants will be joined to teams according to the preset but
     * any participants not mentioned in preset will be ignored/unchanged.
     */
    @Builder.Default
    private final boolean override = false;
    /**
     * if true, all scores will be set to 0 for all teams mentioned in the preset,
     * even if the teams already exist.
     */
    @Builder.Default
    private final boolean resetScores = false;
    /**
     * if true, all participants in the preset will be whitelisted.
     * If false, no participants will be whitelisted by this process.
     */
    @Builder.Default
    private final boolean whiteList = false;
    /**
     * if true, all participants will be un-whitelisted before the preset
     * is applied. If false, no players will be un-whitelisted by this process.
     */
    @Builder.Default
    private final boolean unWhitelist = false;
    /**
     * kick any players which are online but aren't whitelisted after
     * the application of the given preset
     */
    @Builder.Default
    private final boolean kickUnWhitelisted = false;
    
    public boolean override() {
        return override;
    }
    
    public boolean resetScores() {
        return resetScores;
    }
    
    public boolean whiteList() {
        return whiteList;
    }
    
    public boolean unWhitelist() {
        return unWhitelist;
    }
    
    public boolean kickUnWhitelisted() {
        return kickUnWhitelisted;
    }
}
