package org.braekpo1nt.mctmanager.commands.mct.team.preset.editor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.manager.SubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CompositeCommandResult;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetStorageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Lets you join a player to the preset by ign
 */
public class PresetJoinSubCommand extends SubCommand {
    
    
    private final PresetStorageUtil storageUtil;
    
    public PresetJoinSubCommand(PresetStorageUtil storageUtil, @NotNull String name) {
        super(name);
        this.storageUtil = storageUtil;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 3) {
            return CommandResult.failure(getUsage().of("<team>").of("<member>"));
        }
        String presetFile = args[0];
        String teamId = args[1];
        String playerName = args[2];
        return joinParticipant(presetFile, playerName, teamId);
    }
    
    private @NotNull CommandResult joinParticipant(@NotNull String presetFile, @NotNull String ign, @NotNull String teamId) {
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
