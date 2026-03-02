package org.braekpo1nt.mctmanager.commands.manager.brigadier;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

public class MCTCommand2 implements BrigadierCommand {
    
    private final @NotNull GameManager gameManager;
    
    public MCTCommand2(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("mct2")
                .then(new EventSubCommand(gameManager).create())
                .build();
    }
}
