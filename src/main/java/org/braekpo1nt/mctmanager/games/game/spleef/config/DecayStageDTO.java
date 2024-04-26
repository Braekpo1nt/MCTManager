package org.braekpo1nt.mctmanager.games.game.spleef.config;

import lombok.Getter;
import org.braekpo1nt.mctmanager.games.game.spleef.DecayStage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class DecayStageDTO {
    /**
     * info about which layers should decay in this stage and at what rate (empty list for no layers)
     */
    List<LayerInfoDTO> layerInfos;
    /**
     * the duration (in seconds) for which this stage should last. The stage may not last the entire duration if the minPlayers is breached.
     */
    int duration;
    /**
     * the minimum number of participants for this stage. Negative value means there is no minimum number of players and the stage will not proceed until some other requirement is met.
     */
    int minParticipants;
    /**
     * the minimum percentage of participants for this stage. Values between 0 and 1. Negative value means there is no minimum percent of players and the stage will not proceed until some other requirement is met. (defaults to -1)
     */
    double minParticipantsPercent = -1;
    /**
     * whether powerups should be given during this phase (defaults to true)
     */
    boolean powerups = true;
    /**
     * the message to print to the chat when the phase begins (null means no message will be displayed)
     */
    String startMessage;
    
    /**
     * @param index the index of the layer to decay (0-based, must be at least 0 and no more than 1 less than the number of layers in the game)
     * @param blocksPerSecond the rate at which the blocks should decay in Blocks Per Second (must be at least 0)
     */
    record LayerInfoDTO(int index, int blocksPerSecond) {
        DecayStage.LayerInfo toLayerInfo() {
            return new DecayStage.LayerInfo(index, blocksPerSecond);
        }
        
        static List<DecayStage.LayerInfo> toLayerInfos(List<LayerInfoDTO> layerInfoDTOS) {
            return layerInfoDTOS.stream().map(LayerInfoDTO::toLayerInfo).collect(Collectors.toCollection(ArrayList::new));
        }
    }
    
    DecayStage toDecayStage() {
        return new DecayStage(LayerInfoDTO.toLayerInfos(layerInfos), duration, minParticipants, minParticipantsPercent, powerups, startMessage);
    }
    
    static List<DecayStage> toDecayStages(List<DecayStageDTO> decayStageDTOS) {
        return decayStageDTOS.stream().map(DecayStageDTO::toDecayStage).collect(Collectors.toCollection(ArrayList::new));
    }
    
}
