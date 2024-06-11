package org.braekpo1nt.mctmanager.commands.mct.team.preset.editor;

import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetController;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * lets you leave players from their team by ign
 */
public class PresetLeaveSubCommand extends TabSubCommand {
    
    
    private final PresetController controller;
    
    public PresetLeaveSubCommand(PresetController controller, @NotNull String name) {
        super(name);
        this.controller = controller;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return List.of();
    }
}
