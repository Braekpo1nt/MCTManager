package org.braekpo1nt.mctmanager.games.game.mecha.config;

import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;

import java.util.List;

/**
 * @param center            the center of the world border
 * @param initialBorderSize The size that the border should start at
 * @param damageAmount      the amount of damage a player takes when outside the border plus the border buffer.
 * @param damageBuffer      the amount of blocks a player may safely be outside the border before taking damage.
 * @param warningDistance   the warning distance that causes the screen to be tinted red when the player is within the specified number of blocks from the border.
 * @param warningTime       the warning time that causes the screen to be tinted red when a contracting border will reach the player within the specified time.
 * @param borderStages      The stages the border should progress through
 */
record BorderDTO(
        Center center,
        double initialBorderSize,
        double damageAmount,
        double damageBuffer,
        int warningDistance,
        int warningTime,
        List<BorderStage> borderStages) implements Validatable {
    
    @Override
    public void validate(Validator validator) {
        validator.validate(this.center != null, "border.center can't be null");
        validator.validate(this.initialBorderSize >= 1.0,
                "border.initialBorderSize can't be less than 1.0: %s", this.initialBorderSize());
        validator.validate(this.borderStages != null,
                "border.borderStages can't be null");
        validator.validate(this.borderStages.size() >= 1,
                "border.borderStages must have at least one stage");
        for (int i = 0; i < this.borderStages.size(); i++) {
            BorderStage borderStage = borderStages.get(i);
            borderStage.validate(validator.path("borderStages[%d]", i));
        }
    }
    
    record Center(double x, double z) {
    }
    
    /**
     * @param size     The size (in blocks) the border will be at this stage. The border will shrink from the previous stage's size to this stage's size over this stage's duration
     * @param delay    the border will stay at the previous stage's size for this many seconds
     * @param duration the border will take this many seconds to transition from the previous stage's size to this stage's size
     */
    record BorderStage(int size, int delay, int duration) implements Validatable {
        @Override
        public void validate(Validator validator) {
            validator.validate(this.size >= 1.0,
                    "borderStage size (%s) can't be less than 1.0", this.size);
            validator.validate(this.delay >= 0,
                    "borderStage.delay (%S) can't be negative", this.delay);
            validator.validate(this.duration >= 0,
                    "borderStage.duration (%S) can't be negative", this.duration);
        }
    }
}
