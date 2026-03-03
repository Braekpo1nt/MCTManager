package org.braekpo1nt.mctmanager.commands.manager.brigadier.team;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.jetbrains.annotations.NotNull;

public class ListSubCommand implements BrigadierSubCommand {
    
    private final @NotNull Main plugin;
    private final @NotNull GameManager gameManager;
    
    public ListSubCommand(@NotNull Main plugin, @NotNull GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("list")
                .executes(BrigadierAdapters.wraps(ctx -> executeList(false)))
                .then(Commands.argument("showAll", BoolArgumentType.bool())
                        .executes(BrigadierAdapters.wraps(ctx -> {
                            boolean showAll = ctx.getArgument("showAll", Boolean.class);
                            return executeList(showAll);
                        }))
                )
                ;
    }
    
    private CommandResult executeList(boolean showAll) {
        Component teamDisplay = GameManagerUtils.getTeamDisplay(gameManager);
        if (showAll) {
            plugin.getServer().sendMessage(teamDisplay);
            return CommandResult.success();
        } else {
            return CommandResult.success(teamDisplay);
        }
    }
}
