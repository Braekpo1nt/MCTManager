package org.braekpo1nt.mctmanager.commands.manager.brigadier.team.preset;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetStorageUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class PresetRemoveSubCommand implements BrigadierSubCommand {
    
    private final @NotNull PresetStorageUtil storageUtil;
    
    public PresetRemoveSubCommand(@NotNull PresetStorageUtil storageUtil) {
        this.storageUtil = storageUtil;
    }
    
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("remove")
                .then(Commands.argument("teamId", StringArgumentType.word())
                        .suggests((source, builder) -> PresetCommand.suggestPresetTeams(source, builder, storageUtil))
                        .executes(BrigadierAdapters.wraps(this::executeRemove))
                )
                ;
    }
    
    private @NotNull CommandResult executeRemove(CommandContext<CommandSourceStack> ctx) {
        File presetFile = ctx.getArgument(PresetCommand.PRESET_FILE_ARG, File.class);
        String teamId = ctx.getArgument("teamId", String.class);
        
        return removeTeam(presetFile, teamId);
    }
    
    private @NotNull CommandResult removeTeam(@NotNull File presetFile, @NotNull String teamId) {
        return storageUtil.modifyPreset(presetFile, preset -> {
            if (!preset.hasTeamId(teamId)) {
                return CommandResult.failure(Component.text("Team ")
                        .append(Component.text(teamId)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" does not exist in the preset")));
            }
            
            preset.removeTeam(teamId);
            return CommandResult.success(Component.empty()
                    .append(Component.text("Removed "))
                    .append(Component.text(teamId)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" from preset"))
            );
        });
    }
}
