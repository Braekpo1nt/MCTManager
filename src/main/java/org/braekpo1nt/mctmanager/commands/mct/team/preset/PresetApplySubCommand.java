package org.braekpo1nt.mctmanager.commands.mct.team.preset;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.commands.argumenttypes.FileResolver;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetStorageUtil;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class PresetApplySubCommand implements BrigadierSubCommand {
    
    private final @NotNull Main plugin;
    private final @NotNull GameManager gameManager;
    private final @NotNull PresetStorageUtil storageUtil;
    
    public PresetApplySubCommand(@NotNull Main plugin, @NotNull GameManager gameManager, @NotNull PresetStorageUtil storageUtil) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.storageUtil = storageUtil;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("apply")
                .then(Permissioned.argument("override", BoolArgumentType.bool())
                        .then(Permissioned.argument("resetScores", BoolArgumentType.bool())
                                .then(Permissioned.argument("whiteList", BoolArgumentType.bool())
                                        .then(Permissioned.argument("unWhitelist", BoolArgumentType.bool())
                                                .then(Permissioned.argument("kickUnWhitelisted", BoolArgumentType.bool())
                                                        .executes(BrigadierAdapters.wraps(this::executeApply))
                                                )
                                        )
                                )
                        )
                )
                ;
    }
    
    private @NotNull CommandResult executeApply(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        FileResolver resolver = ctx.getArgument(PresetCommand.PRESET_FILE_ARG, FileResolver.class);
        File presetFile = resolver.resolve();
        boolean override = ctx.getArgument("override", Boolean.class);
        boolean resetScores = ctx.getArgument("resetScores", Boolean.class);
        boolean whiteList = ctx.getArgument("whiteList", Boolean.class);
        boolean unWhitelist = ctx.getArgument("unWhitelist", Boolean.class);
        boolean kickUnWhitelisted = ctx.getArgument("kickUnWhitelisted", Boolean.class);
        return GameManagerUtils.applyPreset(
                plugin,
                gameManager,
                storageUtil,
                presetFile,
                override,
                resetScores,
                whiteList,
                unWhitelist,
                kickUnWhitelisted
        );
    }
}
