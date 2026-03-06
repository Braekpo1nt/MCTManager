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

public class PresetLeaveSubCommand implements BrigadierSubCommand {
    
    private final @NotNull PresetStorageUtil storageUtil;
    
    public PresetLeaveSubCommand(@NotNull PresetStorageUtil storageUtil) {
        this.storageUtil = storageUtil;
    }
    
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("leave")
                .then(Commands.argument("member", StringArgumentType.word())
                        .suggests((ctx, builder) -> PresetCommand.suggestPresetParticipants(ctx, builder, storageUtil))
                        .executes(BrigadierAdapters.wraps(this::executeLeave))
                )
                ;
    }
    
    private @NotNull CommandResult executeLeave(CommandContext<CommandSourceStack> ctx) {
        File presetFile = ctx.getArgument(PresetCommand.PRESET_FILE_ARG, File.class);
        String ign = ctx.getArgument("member", String.class);
        return leavePlayer(presetFile, ign);
    }
    
    private @NotNull CommandResult leavePlayer(@NotNull File presetFile, @NotNull String ign) {
        if (ign.isEmpty()) {
            return CommandResult.failure("Player name can't be blank");
        }
        
        return storageUtil.modifyPreset(presetFile, preset -> {
            if (!preset.hasMember(ign)) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text(ign)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is not in the preset"))
                );
            }
            preset.leaveMember(ign);
            return CommandResult.success(Component.empty()
                    .append(Component.text("Left "))
                    .append(Component.text(ign)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" from the preset"))
            );
        });
    }
}
