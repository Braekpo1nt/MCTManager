package org.braekpo1nt.mctmanager.commands.mct.team.preset;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.manager.SubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.gamestate.preset.Preset;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetController;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Retrieves the saved preset file and performs the equivalent 
 * of executing the commands in order to achieve the specified GameState. 
 */
public class PresetApplySubCommand extends SubCommand {
    
    private final PresetController controller;
    private final GameManager gameManager;
    
    public PresetApplySubCommand(GameManager gameManager, PresetController controller, @NotNull String name) {
        super(name);
        this.gameManager = gameManager;
        this.controller = controller;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Preset preset;
        try {
            preset = controller.getPreset();
        } catch (ConfigException e) {
            Bukkit.getLogger().severe(String.format("Could not load preset. %S", e.getMessage()));
            e.printStackTrace();
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Error occurred loading preset. See console for details: "))
                    .append(Component.text(e.getMessage())));
        }
        
        // check if they want to overwrite or merge the game state
        // create all the teams
        
        List<CommandResult> commandResults = new ArrayList<>(preset.getTeamCount() + preset.getParticipantCount());
        for (Preset.PresetTeam team : preset.getTeams()) {
            CommandResult commandResult = GameManagerUtils.addTeam(gameManager, team.getTeamId(), team.getDisplayName(), team.getColor());
            commandResults.add(commandResult);
        }
        
        // add all the members
        for (Preset.PresetTeam team : preset.getTeams()) {
            
        }
        
        return CommandResult.success();
    }
}
