package org.braekpo1nt.mctmanager.games.game.spleef;

import java.util.List;

/**
 * 
 * @param layerInfos info about which layers should decay in this stage and at what rate (empty list for no layers)
 * @param duration the duration (in seconds) for which this stage should last. The stage may not last the entire duration if the minPlayers is breached.
 * @param minParticipants the minimum number of participants for this stage. Negative value means there is no minimum number of players and the stage will not proceed until the duration is up.
 * @param startMessage the message to print to the chat when the phase begins
 */
public record DecayStage(List<LayerInfo> layerInfos, int duration, int minParticipants, String startMessage) {
    /**
     * @param index the index of the layer to decay (0-based, must be at least 0 and no more than 1 less than the number of layers in the game)
     * @param blocksPerSecond the rate at which the blocks should decay in Blocks Per Second (must be at least 0)
     */
    public record LayerInfo(int index, int blocksPerSecond) {
        
    }
}
