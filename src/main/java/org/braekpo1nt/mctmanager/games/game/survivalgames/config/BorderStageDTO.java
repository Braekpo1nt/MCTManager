package org.braekpo1nt.mctmanager.games.game.survivalgames.config;

import lombok.Data;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.survivalgames.BorderStage;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
class BorderStageDTO implements Validatable {
    /**
     * The size (in blocks) the border will be at this stage. The border will shrink from the previous stage's size to this stage's size over this stage's duration
     */
    private int size;
    /**
     * the border will stay at the previous stage's size for this many seconds
     */
    private int delay;
    /**
     * the border will take this many seconds to transition from the previous stage's size to this stage's size
     */
    private int duration;
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.validate(this.size >= 1.0,
                "size (%s) can't be less than 1.0", this.size);
        validator.validate(this.delay >= 0,
                "delay (%S) can't be negative", this.delay);
        validator.validate(this.duration >= 0,
                "duration (%S) can't be negative", this.duration);
    }
    
    public BorderStage toBorderStage() {
        return BorderStage.builder()
                .size(this.size)
                .delay(this.delay)
                .duration(this.duration)
                .build();
    }
    
    public static List<BorderStage> toBorderStages(List<BorderStageDTO> dtos) {
        return dtos.stream()
                .map(BorderStageDTO::toBorderStage)
                .toList();
    }
}
