package org.braekpo1nt.mctmanager.games.game.spleef;

import lombok.*;
import org.bukkit.block.Block;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DecayStage {
    
    /**
     * info about which layers should decay in this stage and at what rate (empty list for no layers)
     */
    private List<LayerInfo> layerInfos;
    /**
     * the duration (in seconds) for which this stage should last. The stage may not last the entire duration if the minPlayers is breached.
     */
    private int duration;
    /**
     * the minimum number of participants for this stage. Negative value means there is no minimum number of players and the stage will not proceed until some other requirement is met.
     */
    private int minParticipants;
    /**
     * the minimum percentage of participants for this stage. Values between 0 and 1. Negative value means there is no minimum percent of players and the stage will not proceed until some other requirement is met. (defaults to -1)
     */
    private double minParticipantsPercent = -1;
    /**
     * whether powerups should be given during this phase (defaults to true)
     */
    private boolean powerups = true;
    /**
     * the message to print to the chat when the phase begins (null means no message will be displayed)
     */
    private String startMessage;
    
    /**
     * sets the solid and decaying block lists to null to save space
     */
    public void clearBlocks() {
        for (LayerInfo layerInfo : layerInfos) {
            layerInfo.setSolidBlocks(null);
            layerInfo.setDecayingBlocks(null);
        }
    }
    
    @Getter
    @RequiredArgsConstructor
    public static class LayerInfo {
        /**
         * The index of the layer to decay (0-based, must be at least 0 and no more than 1 less than the number of layers in the game)
         */
        private final int index;
        /**
         * the rate at which the blocks should decay in Blocks Per Second (must be at least 0)
         */
        private final int blocksPerSecond;
        /**
         * The blocks in this layer which are solid, not decayed yet
         */
        @Setter
        private List<Block> solidBlocks;
        /**
         * The blocks in this layer which are partially decayed, about to disappear
         */
        @Setter
        private List<Block> decayingBlocks;
    }
    
    /**
     * @return true if powerups should be given during this phase, false if not. 
     */
    public boolean shouldGivePowerups() {
        return powerups;
    }
}
