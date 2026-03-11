package org.braekpo1nt.mctmanager.commands.mct.event;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.commands.argumenttypes.EventInfoArgumentType;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

public class ModifyCommand implements BrigadierSubCommand {
    
    public final @NotNull GameManager gameManager;
    
    public ModifyCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("modify")
                .then(Permissioned.argument("eventId", new EventInfoArgumentType(gameManager.getEventService()))
                        .then(Permissioned.literal("eventDate")
                                .then(Permissioned.argument("date", StringArgumentType.word())
                                        .suggests(EventSubCommand::suggestDate)
                                        .executes(BrigadierAdapters.wraps(this::executeModifyEventDate))
                                )
                        )
                        .then(Permissioned.literal("componentName")
                                .then(Permissioned.argument("component", gameManager.getComponentArgumentType())
                                        .executes(BrigadierAdapters.wraps(this::executeModifyComponentName))
                                )
                        )
                )
                ;
    }
    
    private @NotNull CommandResult executeModifyEventDate(CommandContext<CommandSourceStack> ctx) {
        return null;
    }
    
    private @NotNull CommandResult executeModifyComponentName(@NotNull CommandContext<CommandSourceStack> ctx) {
        return null;
    }
}
