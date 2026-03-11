package org.braekpo1nt.mctmanager.commands.mct.team.preset;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.commands.argumenttypes.FileResolver;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CompositeCommandResult;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetStorageUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PresetJoinSubCommand implements BrigadierSubCommand {
    
    private final @NotNull Main plugin;
    private final @NotNull PresetStorageUtil storageUtil;
    
    public PresetJoinSubCommand(@NotNull Main plugin, @NotNull PresetStorageUtil storageUtil) {
        this.plugin = plugin;
        this.storageUtil = storageUtil;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("join")
                .then(Permissioned.argument("teamId", StringArgumentType.word())
                        .suggests((source, builder) -> PresetCommand.suggestPresetTeams(source, builder, storageUtil))
                        .then(Permissioned.argument("member", StringArgumentType.word())
                                .suggests((ctx, builder) -> PresetCommand.suggestPresetCandidates(ctx, builder, storageUtil, plugin))
                                .executes(BrigadierAdapters.wraps(this::executeJoin))
                        )
                )
                ;
    }
    
    private @NotNull CommandResult executeJoin(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        FileResolver resolver = ctx.getArgument(PresetCommand.PRESET_FILE_ARG, FileResolver.class);
        File presetFile = resolver.resolve();
        String teamId = ctx.getArgument("teamId", String.class);
        String ign = ctx.getArgument("member", String.class);
        return joinParticipant(presetFile, ign, teamId);
    }
    
    private @NotNull CommandResult joinParticipant(@NotNull File presetFile, @NotNull String ign, @NotNull String teamId) {
        if (ign.isEmpty()) {
            return CommandResult.failure("player name must not be blank");
        }
        if (teamId.isEmpty()) {
            return CommandResult.failure("teamId must not be blank");
        }
        
        return storageUtil.modifyPreset(presetFile, preset -> {
            if (!preset.hasTeamId(teamId)) {
                return CommandResult.failure(Component.text("Team ")
                        .append(Component.text(teamId)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" does not exist in preset")));
            }
            
            List<CommandResult> results = new ArrayList<>(2);
            if (preset.hasMember(ign)) {
                String previousTeamId = preset.getMemberTeamId(ign);
                if (previousTeamId != null) {
                    if (previousTeamId.equals(teamId)) {
                        return CommandResult.success(Component.empty()
                                .append(Component.text(ign)
                                        .decorate(TextDecoration.BOLD))
                                .append(Component.text(" is already on team "))
                                .append(Component.text(teamId)
                                        .decorate(TextDecoration.BOLD)));
                    } else {
                        preset.leaveMember(ign);
                        results.add(CommandResult.success(Component.empty()
                                .append(Component.text("Left "))
                                .append(Component.text(ign)
                                        .decorate(TextDecoration.BOLD))
                                .append(Component.text(" from "))
                                .append(Component.text(previousTeamId)
                                        .decorate(TextDecoration.BOLD))
                                .append(Component.text(" in the preset"))
                        ));
                    }
                }
            }
            
            preset.joinMember(ign, teamId);
            
            results.add(CommandResult.success(Component.empty()
                    .append(Component.text("Joined "))
                    .append(Component.text(ign)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" to "))
                    .append(Component.text(teamId)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" in the preset"))
            ));
            return CompositeCommandResult.all(results);
        });
    }
}
