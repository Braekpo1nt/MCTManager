package org.braekpo1nt.mctmanager.commands.mct.team.preset;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.commands.argumenttypes.FileResolver;
import org.braekpo1nt.mctmanager.commands.argumenttypes.GreedyListArgumentType;
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
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

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
                .then(Permissioned.argument("options", GreedyListArgumentType.of(
                                Set.of(
                                        "override",
                                        "resetScores",
                                        "whiteList",
                                        "unWhitelist",
                                        "kickUnWhitelisted"
                                )))
                        .executes(BrigadierAdapters.wraps(this::executeApply))
                )
                ;
    }
    
    private @NotNull CommandResult executeApply(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        FileResolver resolver = ctx.getArgument(PresetCommand.PRESET_FILE_ARG, FileResolver.class);
        File presetFile = resolver.resolve();
        String[] optionsArray = ctx.getArgument("options", String[].class);
        Set<String> options = Arrays.stream(optionsArray)
                .collect(Collectors.toSet());
        boolean override = options.contains("override");
        boolean resetScores = options.contains("resetScores");
        boolean whiteList = options.contains("whiteList");
        boolean unWhitelist = options.contains("unWhitelist");
        boolean kickUnWhitelisted = options.contains("kickUnWhitelisted");
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
