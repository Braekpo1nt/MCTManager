package org.braekpo1nt.mctmanager.commands.mct.team.preset;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetStorageUtil;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.*;
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
        
        return GameManagerUtils.applyPreset(plugin, gameManager, storageUtil, presetFile, override, resetScores, whiteList);
    }
    
    private final List<String> validOptions = List.of("override", "resetScores", "whiteList");
    
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
