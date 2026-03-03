package org.braekpo1nt.mctmanager.commands.manager.brigadier.team.preset;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetStorageUtil;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class PresetAddSubCommand implements BrigadierSubCommand {
    
    private final @NotNull PresetStorageUtil storageUtil;
    
    public PresetAddSubCommand(@NotNull PresetStorageUtil storageUtil) {
        this.storageUtil = storageUtil;
    }
    
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("add")
                .then(Commands.argument("teamId", StringArgumentType.word())
                        .then(Commands.argument("displayName", StringArgumentType.string())
                                .then(Commands.argument("color", StringArgumentType.word())
                                        // TODO: create custom command argument
                                        .suggests(CommandUtils::suggestColor)
                                        .executes(BrigadierAdapters.wraps(this::executeAdd))
                                )
                        )
                )
                ;
    }
    
    private CommandResult executeAdd(CommandContext<CommandSourceStack> ctx) {
        File presetFile = ctx.getArgument(PresetCommand.PRESET_FILE_ARG, File.class);
        String teamId = ctx.getArgument("teamId", String.class);
        String displayName = ctx.getArgument("displayName", String.class);
        String colorString = ctx.getArgument("color", String.class);
        
        return addTeam(presetFile, teamId, displayName, colorString);
    }
    
    private @NotNull CommandResult addTeam(@NotNull File presetFile, @NotNull String teamId, @NotNull String displayName, @NotNull String colorString) {
        return storageUtil.modifyPreset(presetFile, preset -> {
            if (preset.hasTeamId(teamId)) {
                return CommandResult.failure(Component.text("A team already exists with the teamId \"")
                        .append(Component.text(teamId))
                        .append(Component.text("\" in the preset")));
            }
            
            if (teamId.equals(GameManager.ADMIN_TEAM)) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text(teamId)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" cannot be "))
                        .append(Component.text(GameManager.ADMIN_TEAM)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" because that is reserved for the admin team.")));
            }
            
            if (!GameManagerUtils.validTeamId(teamId)) {
                return CommandResult.failure(Component.text("Provide a valid team name\n")
                        .append(Component.text(
                                "Allowed characters: -, +, ., _, A-Z, a-z, and 0-9")));
            }
            
            if (displayName.isEmpty()) {
                return CommandResult.failure("Display name can't be blank");
            }
            
            if (!ColorMap.hasNamedTextColor(colorString)) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text(colorString)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is not a recognized color")));
            }
            
            preset.addTeam(teamId, displayName, colorString);
            return CommandResult.success(Component.empty()
                    .append(Component.text("Added "))
                    .append(Component.text(displayName)
                            .color(ColorMap.getNamedTextColor(colorString)))
                    .append(Component.text(" to the preset")));
        });
    }
}
