package org.braekpo1nt.mctmanager.games.game.spleef.config;

import com.google.common.base.Preconditions;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.NamespacedKeyDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.util.BoundingBoxDTO;
import org.braekpo1nt.mctmanager.config.validation.ConfigInvalidException;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.Bukkit;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

/**
 * @param structure       the NamespacedKey of the structure to place for this layer
 * @param structureOrigin the origin to place the structure at
 * @param decayArea       the area in which to decay blocks for this layer. If this is null, the size of the structure and structureOrigin will be used as the area.
 */
record LayerDTO(@Nullable NamespacedKeyDTO structure, Vector structureOrigin,
                @Nullable BoundingBoxDTO decayArea) implements Validatable {
    @Override
    public void validate(Validator validator) throws ConfigInvalidException {
        validator.validate(this.structure() != null, "layer.structure can't be null");
        Preconditions.checkArgument(this.structure != null);
        this.structure.validate(validator);
        validator.validate(Bukkit.getStructureManager().loadStructure(this.structure.toNamespacedKey()) != null, "Can't find structure %s", this.structure());
        validator.validate(this.structureOrigin() != null, "layer.structureOrigin can't be null");
    }
}
