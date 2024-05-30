package org.braekpo1nt.mctmanager.games.game.clockwork;

import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.jetbrains.annotations.NotNull;

public record Chaos(
        Cylinder cylinder, 
        MinMaxInc arrows, 
        MinMaxInc fallingBlocks, 
        MinMaxDec summonDelay,
                    
        MinMaxFloat arrowSpeed, 
        MinMaxFloat arrowSpread) implements Validatable {
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(this.cylinder(), "cylinder");
        validator.notNull(this.cylinder().spawnY(), "cylinder.spawnY");
        validator.validate(this.cylinder().spawnY().min() < this.cylinder().spawnY().max(), "cylinder.spawnY min must be less than max");
    
        validator.notNull(this.arrows(), "arrows");
        validator.notNull(this.arrows().initial(), "arrows.initial");
        validator.notNull(this.arrows().increment(), "arrows.increment");
        validator.validate(((int) this.arrows().initial().min()) < ((int) this.arrows().initial().max())+1, "arrows.initial floor(min) must be less than floor(max)+1");
        validator.validate(this.arrows().increment().min() <= this.arrows().increment().max(), "arrows.increment min must be less than or equal to max");
    
        validator.notNull(this.fallingBlocks(), "fallingBlocks");
        validator.notNull(this.fallingBlocks().initial(), "fallingBlocks.initial");
        validator.notNull(this.fallingBlocks().increment(), "fallingBlocks.increment");
        validator.validate(((int) this.fallingBlocks().initial().min()) < ((int) this.fallingBlocks().initial().max())+1, "fallingBlocks.initial floor(min) must be less than floor(max)+1");
        validator.validate(this.fallingBlocks().increment().min() <= this.fallingBlocks().increment().max(), "fallingBlocks.increment min must be less than or equal to max");
    
        validator.notNull(this.summonDelay(), "summonDelay");
        validator.notNull(this.summonDelay().initial(), "summonDelay.initial");
        validator.notNull(this.summonDelay().decrement(), "summonDelay.decrement");
        validator.validate(this.summonDelay().initial().min() >= 5, "summonDelay.initial.min must be greater than or equal to 5");
        validator.validate(((long) this.summonDelay().initial().min()) < ((long) this.summonDelay().initial().min() + 1), "summonDelay.initial floor(min) must be less than floor(max)+1");
        validator.validate(this.summonDelay().decrement().min() <= this.summonDelay().decrement().max(), "summonDelay.decrement min must be less than or equal to max");
    
        validator.notNull(this.arrowSpeed(), "arrowSpeed");
        validator.validate(this.arrowSpeed().min() >= 0f, "arrowSpeed.min can't be negative");
        validator.validate(this.arrowSpeed().max() >= 0f, "arrowSpeed.max can't be negative");
    
        validator.notNull(this.arrowSpread(), "arrowSpread");
        validator.validate(this.arrowSpread().min() >= 0f, "arrowSpread.min can't be negative");
        validator.validate(this.arrowSpread().max() >= 0f, "arrowSpread.max can't be negative");
    }
    
    public record Cylinder(double centerX, double centerZ, double radius, MinMax spawnY) {
    }
    
    public record MinMax(double min, double max) {
    }
    
    public record MinMaxFloat(float min, float max) {
    }
    
    public record MinMaxInc(MinMax initial, MinMax increment) {
    }
    
    public record MinMaxDec(MinMax initial, MinMax decrement) {
    }
}
