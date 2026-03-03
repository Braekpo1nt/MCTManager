package org.braekpo1nt.mctmanager.commands.manager.brigadier.team.preset;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.argumenttypes.FileArgumentType;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetStorageUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class PresetCommand implements BrigadierSubCommand {
    
    public static final String PRESET_FILE_ARG = "presetFile";
    private final @NotNull Main plugin;
    private final @NotNull GameManager gameManager;
    
    public PresetCommand(@NotNull Main plugin, @NotNull GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        PresetStorageUtil storageUtil = new PresetStorageUtil(plugin.getDataFolder());
        return Commands.literal("preset")
                .then(Commands.argument(PRESET_FILE_ARG,
                                new FileArgumentType(new File(plugin.getDataFolder(), "presets"), ".json"))
                        .then(new PresetApplySubCommand(plugin, gameManager, storageUtil).create())
                        .then(new PresetWhitelistSubCommand(storageUtil).create())
                        // editor:
                        .then(new PresetAddSubCommand(storageUtil).create())
                )
                ;
    }
}
