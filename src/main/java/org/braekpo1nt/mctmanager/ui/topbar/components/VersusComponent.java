package org.braekpo1nt.mctmanager.ui.topbar.components;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents two teams battling
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class VersusComponent {
    /**
     * The TeamComponent to be displayed to the left of the "vs"
     */
    private final @NotNull TeamComponent left;
    /**
     * The TeamComponent to be displayed to the right of the "vs"
     */
    private @Nullable TeamComponent right;
    
    /**
     * @return a {@link Component}-ized representation of this VersusComponent,
     * including the left and right {@link TeamComponent}s
     */
    public @NotNull Component toComponent() {
        if (right == null) {
            return Component.empty()
                    .append(left.toComponent());
        }
        return Component.empty()
                .append(left.toComponent())
                .append(Component.text(" VS "))
                .append(right.toComponent())
                ;
    }
}
