package org.braekpo1nt.mctmanager.commands.mctdebug;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MigrateDebugCommand implements BrigadierSubCommand {
    
    
    private final @NotNull GameManager gameManager;
    private final @NotNull Main plugin;
    
    public MigrateDebugCommand(@NotNull GameManager gameManager, @NotNull Main plugin) {
        this.gameManager = gameManager;
        this.plugin = plugin;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("migrate")
                .then(Permissioned.literal("uuid")
                        .then(Permissioned.argument("fromUUID", plugin.getUUIDArgumentType())
                                .then(Permissioned.argument("toUUID", plugin.getUUIDArgumentType())
                                        .then(Permissioned.argument("ign", StringArgumentType.word())
                                                .executes(BrigadierAdapters.wraps(this::executeMigrateUUID))
                                        )
                                )
                        )
                )
                .then(Permissioned.literal("ign")
                        .then(Permissioned.argument("uuid", plugin.getUUIDArgumentType())
                                .then(Permissioned.argument("newIGN", StringArgumentType.word())
                                        .executes(BrigadierAdapters.wraps(this::executeMigrateIGN))
                                )
                        )
                );
    }
    
    private @NotNull CommandResult executeMigrateUUID(CommandContext<CommandSourceStack> ctx) {
        UUID fromUuid = ctx.getArgument("fromUUID", UUID.class);
        UUID toUuid = ctx.getArgument("toUUID", UUID.class);
        String ign = ctx.getArgument("ign", String.class);
        return gameManager.migrateUUID(fromUuid, toUuid, ign);
    }
    
    private @NotNull CommandResult executeMigrateIGN(CommandContext<CommandSourceStack> ctx) {
        UUID uuid = ctx.getArgument("uuid", UUID.class);
        String toIGN = ctx.getArgument("newIGN", String.class);
        return gameManager.migrateIGN(uuid, toIGN);
    }
}
