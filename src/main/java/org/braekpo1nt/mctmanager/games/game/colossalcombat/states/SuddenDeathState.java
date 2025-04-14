package org.braekpo1nt.mctmanager.games.game.colossalcombat.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalCombatGame;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.jetbrains.annotations.NotNull;

public class SuddenDeathState extends GameplayState {
    public SuddenDeathState(@NotNull ColossalCombatGame context) {
        super(context);
        context.titleAllParticipants(UIUtils.defaultTitle(
                Component.empty()
                        .append(Component.text("Sudden Death"))
                        .color(NamedTextColor.DARK_PURPLE),
                Component.empty()
                        .append(Component.text("Capture the Flag"))
                        .color(NamedTextColor.DARK_PURPLE)
        ));
        context.messageAllParticipants(Component.empty()
                .append(Component.text("Sudden death has begun. Capture the flag to win!"))
                .color(NamedTextColor.DARK_PURPLE));
    }
}
