package org.braekpo1nt.mctmanager.ui.topbar.components;

import lombok.Data;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Represents two teams battling
 */
@Data
public class VersusComponent {
    /**
     * The TeamComponent to be displayed to the left of the "vs"
     */
    private final @NotNull TeamComponent left;
    /**
     * The TeamComponent to be displayed to the right of the "vs"
     */
    private final @NotNull TeamComponent right;
    
    /**
     * @return a {@link Component}-ized representation of this VersusComponent,
     * including the left and right {@link TeamComponent}s
     */
    public @NotNull Component toComponent() {
        return Component.empty()
                .append(left.toComponent())
                .append(Component.text(" VS "))
                .append(right.toComponent())
                ;
    }
}
