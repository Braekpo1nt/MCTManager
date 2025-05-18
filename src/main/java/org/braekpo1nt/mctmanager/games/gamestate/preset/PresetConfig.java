package org.braekpo1nt.mctmanager.games.gamestate.preset;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PresetConfig {
    private String file;
    private boolean override;
    private boolean resetScores;
    /**
     * Add participants who are in the preset to the whitelist when applied
     */
    private boolean whitelist;
    /**
     * Remove current participants from the whitelist before the whitelist
     * is applied
     */
    private boolean unWhitelist;
    /**
     * if true, players who are not whitelisted when transitioning to the practice mode
     * will be kicked (this kicking happens after the application of the preset)
     */
    private boolean kickUnWhitelisted;
}
