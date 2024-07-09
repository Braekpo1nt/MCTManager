package org.braekpo1nt.mctmanager.games.game.spleef.config;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.spleef.DecayStage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class DecayStageDTO implements Validatable {
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
    Component startMessage;
    /**
     * the subtitle to show when the phase begins (null means no subtitle will be displayed)
     */
    Component startSubtitle;
    
    @Data
    static class LayerInfoDTO implements Validatable {
        /**
         * The index of the layer to decay (0-based, must be at least 0 and no more than 1 less than the number of layers in the game)
         */
        private int index;
        /**
         * the rate at which the solid blocks should decay in Blocks Per Second. Must be at least 0. Defaults to 0.
         */
        @SerializedName(value = "solidBlockRate", alternate = {"blocksPerSecond"})
        private int solidBlockRate = 0;
        /**
         * the rate at which the decaying blocks should disappear in Blocks Per Second. Defaults to -1. If this value is less than 0, it will be assigned to the value of {@link LayerInfoDTO#solidBlockRate}.
         */
        private int decayingBlockRate = -1;
    
        @Override
        public void validate(@NotNull Validator validator) {
            validator.validate(0 <= solidBlockRate, "solidBlockRate must be can't be negative");
            validator.validate(0 <= index, "index can't be negative");
        }
    
        DecayStage.LayerInfo toLayerInfo() {
            return new DecayStage.LayerInfo(index, solidBlockRate, decayingBlockRate < 0 ? solidBlockRate : decayingBlockRate);
        }
        
        static List<DecayStage.LayerInfo> toLayerInfos(List<LayerInfoDTO> layerInfoDTOS) {
            return layerInfoDTOS.stream().map(LayerInfoDTO::toLayerInfo).collect(Collectors.toCollection(ArrayList::new));
        }
    }
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.validate(this.layerInfos != null, "layers can't be null");
        // make sure index is between 0 and the max index for decayLayers 
        // also make sure there are no duplicate indexes
        Set<Integer> uniqueIndexes = new HashSet<>(this.layerInfos.size());
        for (int i = 0; i < this.layerInfos.size(); i++) {
            DecayStageDTO.LayerInfoDTO layerInfo = this.layerInfos.get(i);
            layerInfo.validate(validator.path("layerInfos[%d]", i));
            validator.validate(!uniqueIndexes.contains(layerInfo.getIndex()), "layerInfos[%d].index must be unique (%s)", i, layerInfo.getIndex());
            uniqueIndexes.add(layerInfo.getIndex());
        }
        validator.validate(this.duration > 0, "duration must be at least 1");
    }
    
    /**
     * 
     * @param validator the validator
     * @param numberOfLayers the number of layers 
     */
    public void validateIndexes(Validator validator, int numberOfLayers) {
        for (int i = 0; i < this.layerInfos.size(); i++) {
            LayerInfoDTO layerInfo = this.layerInfos.get(i);
            validator.validate(0 <= layerInfo.getIndex() && layerInfo.getIndex() < numberOfLayers, "layerInfos[%d].index must be at most 1 less than the number of elements in layers list", i);
        }
    }
    
    DecayStage toDecayStage() {
        return DecayStage.builder()
                .layerInfos(LayerInfoDTO.toLayerInfos(layerInfos))
                .duration(duration)
                .minParticipants(minParticipants)
                .minParticipantsPercent(minParticipantsPercent)
                .powerups(powerups)
                .startMessage(startMessage)
                .startSubtitle(startSubtitle)
                .build();
    }
    
    static List<DecayStage> toDecayStages(List<DecayStageDTO> decayStageDTOS) {
        return decayStageDTOS.stream().map(DecayStageDTO::toDecayStage).collect(Collectors.toCollection(ArrayList::new));
    }
    
}
