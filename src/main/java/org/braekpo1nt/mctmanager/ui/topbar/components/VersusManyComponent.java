package org.braekpo1nt.mctmanager.ui.topbar.components;

import lombok.Data;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Represents one team battling many other teams
 */
@Data
public class VersusManyComponent {
    private final @NotNull TeamComponent friendly;
    private final @NotNull ManyTeamsComponent opponents = new ManyTeamsComponent();
    
    public Component toComponent() {
        return Component.empty()
                .append(friendly.toComponent())
                .append(Component.text(" VS "))
                .append(opponents.toComponent())
                ;
    }
}
