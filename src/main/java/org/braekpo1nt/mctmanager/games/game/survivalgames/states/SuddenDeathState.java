package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesGame;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.jetbrains.annotations.NotNull;

public class SuddenDeathState extends RoundActiveState {
    
    public SuddenDeathState(@NotNull SurvivalGamesGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        super.enter();
        if (config.getBorder().allowRespawn(context.getBorderStageIndex())) {
            context.titleAllParticipants(UIUtils.defaultTitle(
                    Component.empty()
                            .append(Component.text("Respawning Disabled")
                                    .color(NamedTextColor.RED)),
                    Component.empty()
            ));
            context.messageAllParticipants(Component.text("Respawning is disabled")
                    .color(NamedTextColor.RED));
        }
        updateRespawnLine();
        Component message = Component.empty()
                .append(Component.text("Sudden Death")
                        .color(NamedTextColor.RED));
        topbar.setMiddle(message);
        adminSidebar.updateLine("timer", message);
        context.messageAllParticipants(Component.empty()
                .append(Component.text("Sudden death!")
                        .color(NamedTextColor.RED)));
    }
    
    @Override
    protected boolean allowRespawn() {
        return false;
    }
    
    @Override
    protected void updateRespawnLine() {
        if (config.getBorder().neverRespawn()) {
            return;
        }
        Component respawnLine = config.getBorder().getRespawnDisabledLine();
        context.getAdminSidebar().updateLine("respawn", respawnLine);
        context.getSidebar().updateLine("respawn", respawnLine);
    }
}
