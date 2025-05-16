package org.braekpo1nt.mctmanager.commands.mct.team.preset;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CompositeCommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamestate.preset.Preset;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetStorageUtil;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.participant.Team;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Retrieves the saved preset file and performs the equivalent 
 * of executing the commands in order to achieve the specified GameState. 
 */
public class PresetApplySubCommand extends TabSubCommand {
    
    private final PresetStorageUtil storageUtil;
    private final Main plugin;
    private final GameManager gameManager;
    
    public PresetApplySubCommand(Main plugin, GameManager gameManager, PresetStorageUtil storageUtil, @NotNull String name) {
        super(name);
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.storageUtil = storageUtil;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        if (args.length < 1) {
            return CommandResult.failure(getUsage().of("[override|resetScores|whiteList]"));
        }
        
        boolean override = false;
        boolean resetScores = false;
        boolean whiteList = false;
        
        String presetFile = args[0];
        
        Set<String> seenArguments = new HashSet<>();
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if (seenArguments.contains(arg)) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text("Duplicate argument: "))
                        .append(Component.text(arg)
                                .decorate(TextDecoration.BOLD))
                );
            }
            switch (arg) {
                case "override":
                    override = true;
                    break;
                case "resetScores":
                    resetScores = true;
                    break;
                case "whiteList":
                    whiteList = true;
                    break;
                default:
                    return CommandResult.failure(Component.empty()
                            .append(Component.text(arg)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" is not a recognized option")));
            }
            seenArguments.add(arg);
        }
        
        return applyPreset(presetFile, override, resetScores, whiteList);
    }
    
    /**
     * 
     * @param whiteList if true, all players listed in the preset will be whitelisted as well
     * @param override if true, all previous teams and participants will be cleared and the preset 
     *                 teams and participants will be added (thus replacing everything with the 
     *                 preset). If false, the previous GameSate will not be changed, and it will 
     *                 try to add all teams from the preset but not override existing teams, 
     *                 and participants will be joined to teams according to the preset but 
     *                 any participants not mentioned in preset will be ignored/unchanged.
     * @param resetScores if true, all scores will be set to 0 for all teams mentioned in the preset, even if the teams already exist. 
     * @return a comprehensive {@link CompositeCommandResult} including every {@link CommandResult} of the (perhaps many) operations performed here.
     */
    private @NotNull CommandResult applyPreset(@NotNull String presetFile, boolean override, boolean resetScores, boolean whiteList) {
        Preset preset;
        try {
            preset = storageUtil.loadPreset(presetFile);
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Could not load preset. %s", e.getMessage()), e);
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Error occurred loading preset. See console for details: "))
                    .append(Component.text(e.getMessage())));
        }
         
        // check if they want to overwrite or merge the game state
        List<CommandResult> results = new LinkedList<>();
        if (override) {
            // remove all existing teams and leave all existing players
            int oldParticipantCount = gameManager.getOfflineParticipants().size();
            Set<String> teamIds = gameManager.getTeamIds();
            int oldTeamCount = teamIds.size();
            for (String teamId : teamIds) {
                results.add(GameManagerUtils.removeTeam(gameManager, teamId));
            }
            results.add(CommandResult.success(Component.empty()
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
            Team realTeam = gameManager.getTeam(team.getTeamId());
            if (realTeam != null) {
                results.add(CommandResult.success(Component.empty()
                        .append(realTeam.getFormattedDisplayName())
                        .append(Component.text(" already exists."))
                ));
            } else {
                CommandResult commandResult = GameManagerUtils.addTeam(gameManager, team.getTeamId(), team.getDisplayName(), team.getColor());
                results.add(commandResult);
            }
        }
        
        // join all the participants
        for (Preset.PresetTeam team : preset.getTeams()) {
            for (String ign : team.getMembers()) {
                results.add(GameManagerUtils.joinParticipant(plugin, gameManager, ign, team.getTeamId()));
            }
        }
        
        results.add(CommandResult.success(Component.empty()
                .append(Component.text("Successfully added "))
                .append(Component.text(teamCount))
                .append(Component.text(" team(s) and joined "))
                .append(Component.text(participantCount))
                .append(Component.text(" participant(s)."))));
        
        if (resetScores) {
            gameManager.setScoreAll(0);
            results.add(CommandResult.success(Component.empty()
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
            results.add(CommandResult.success(Component.empty()
                    .append(Component.text("Whitelisted "))
                    .append(Component.text(participantCount))
                    .append(Component.text(" participant(s)"))));
        }
        
        return CompositeCommandResult.all(results);
    }
    
    private final List<String> validOptions = Arrays.asList("override", "resetScores", "whiteList");
    
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            return Collections.emptyList();
        }
        Set<String> seenArguments = Arrays.stream(args).collect(Collectors.toSet());
        List<String> suggestions = new ArrayList<>();
        for (String option : validOptions) {
            if (!seenArguments.contains(option)) {
                suggestions.add(option);
            }
        }
        return suggestions;
    }
}
