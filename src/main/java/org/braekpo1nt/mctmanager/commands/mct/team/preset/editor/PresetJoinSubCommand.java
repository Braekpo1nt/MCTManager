package org.braekpo1nt.mctmanager.commands.mct.team.preset.editor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CompositeCommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.gamestate.preset.Preset;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetController;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Lets you join a player to the preset by ign
 */
public class PresetJoinSubCommand extends TabSubCommand {
    
    
    private final PresetController controller;
    
    public PresetJoinSubCommand(PresetController controller, @NotNull String name) {
        super(name);
        this.controller = controller;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            return CommandResult.failure(getUsage().of("<team>").of("<member>"));
        }
        String teamId = args[0];
        String playerName = args[1];
        return joinParticipant(playerName, teamId);
    }
    
    private @NotNull CommandResult joinParticipant(@NotNull String ign, @NotNull String teamId) {
        if (ign.isEmpty()) {
            return CommandResult.failure("player name must not be blank");
        }
        if (teamId.isEmpty()) {
            return CommandResult.failure("teamId must not be blank");
        }
        
        Preset preset;
        try {
            preset = controller.getPreset();
        } catch (ConfigException e) {
            Bukkit.getLogger().severe(String.format("Could not load preset. %s", e.getMessage()));
            e.printStackTrace();
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Error occurred loading preset. See console for details: "))
                    .append(Component.text(e.getMessage())));
        }
        
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
                    ));
                }
            }
        }
        
        preset.joinMember(ign, teamId);
        try {
            controller.savePreset(preset);
        } catch (ConfigException e) {
            Bukkit.getLogger().severe(String.format("Could not save preset. %s", e.getMessage()));
            e.printStackTrace();
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Error occurred saving preset. See console for details: "))
                    .append(Component.text(e.getMessage())));
        }
        results.add(CommandResult.success(Component.empty()
                .append(Component.text("Joined "))
                .append(Component.text(ign)
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" to "))
                .append(Component.text(teamId)
                        .decorate(TextDecoration.BOLD))
        ));
        return CompositeCommandResult.all(results);
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return List.of();
    }
}
