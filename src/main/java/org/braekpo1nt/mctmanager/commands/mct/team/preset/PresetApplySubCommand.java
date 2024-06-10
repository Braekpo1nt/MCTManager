package org.braekpo1nt.mctmanager.commands.mct.team.preset;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.manager.SubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CompositeCommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.gamestate.preset.Preset;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetController;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
        
        
        return applyPreset(sender, false, false, false);
    }
    
    /**
     * 
     * @param sender the sender
     * @param whiteList if true, all players listed in the preset will be whitelisted as well
     * @param override if true, all previous teams and participants will be cleared and the preset teams and participants will be added (thus replacing everything with the preset). If false, the previous GameSate will not be changed, and it will try to add all teams from the preset but not override existing teams, and participants will be joined to teams according to the preset but any participants not mentioned in preset will be ignored/unchanged.
     * @param resetScores if true, all scores will be set to 0 for all teams mentioned in the preset, even if the teams already exist. 
     * @return a comprehensive {@link CompositeCommandResult} including every {@link CommandResult} of the (perhaps many) operations performed here.
     */
    private @NotNull CommandResult applyPreset(@NotNull CommandSender sender, boolean override, boolean resetScores, boolean whiteList) {
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
        List<CommandResult> commandResults = new LinkedList<>();
        if (override) {
            // remove all existing teams and leave all existing players
            int oldParticipantCount = gameManager.getOfflineParticipants().size();
            Set<String> teamIds = gameManager.getTeamNames();
            int oldTeamCount = teamIds.size();
            for (String teamId : teamIds) {
                CommandResult commandResult = GameManagerUtils.removeTeam(sender, gameManager, teamId);
                commandResults.add(commandResult);
            }
            commandResults.add(CommandResult.success(Component.empty()
                    .append(Component.text("Removed "))
                    .append(Component.text(oldTeamCount))
                    .append(Component.text(" team(s) and left "))
                    .append(Component.text(oldParticipantCount))
                    .append(Component.text(" participants"))));
        }
        
        // add all the teams
        int teamCount = preset.getTeamCount();
        int participantCount = preset.getParticipantCount();
        for (Preset.PresetTeam team : preset.getTeams()) {
            CommandResult commandResult = GameManagerUtils.addTeam(gameManager, team.getTeamId(), team.getDisplayName(), team.getColor());
            commandResults.add(commandResult);
        }
        
        // join all the participants
        for (Preset.PresetTeam team : preset.getTeams()) {
            for (String ign : team.getMembers()) {
                CommandResult commandResult = GameManagerUtils.joinParticipant(sender, gameManager, ign, team.getTeamId());
                commandResults.add(commandResult);
            }
        }
        
        commandResults.add(CommandResult.success(Component.empty()
                .append(Component.text("Successfully added "))
                .append(Component.text(teamCount))
                .append(Component.text(" team(s) and joined "))
                .append(Component.text(participantCount))
                .append(Component.text(" participant(s)."))));
        
        if (resetScores) {
            gameManager.setScoreAll(0);
            commandResults.add(CommandResult.success(Component.empty()
                    .append(Component.text("All team and player scores have been set to 0"))));
        }
        
        if (whiteList) {
            for (Preset.PresetTeam team : preset.getTeams()) {
                for (String ign : team.getMembers()) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ign);
                    if (!offlinePlayer.isWhitelisted()) {
                        offlinePlayer.setWhitelisted(true);
                    }
                }
            }
            commandResults.add(CommandResult.success(Component.empty()
                    .append(Component.text("Whitelisted "))
                    .append(Component.text(participantCount))
                    .append(Component.text(" participant(s)"))));
        }
        
        return CompositeCommandResult.all(commandResults);
    }
}
