package org.braekpo1nt.mctmanager.commands.mct.team;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.commands.manager.Usage;
import org.braekpo1nt.mctmanager.commands.mct.team.preset.PresetCommand;
import org.braekpo1nt.mctmanager.commands.mct.team.score.ScoreCommand;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;

public class TeamCommand extends CommandManager {
    
    public TeamCommand(Main plugin, GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new AddSubCommand(gameManager, "add"));
        addSubCommand(new JoinSubCommand(plugin, gameManager, "join"));
        addSubCommand(new LeaveSubCommand(plugin, gameManager, "leave"));
        addSubCommand(new ListSubCommand(gameManager, "list"));
        addSubCommand(new RemoveSubCommand(gameManager, "remove"));
        addSubCommand(new ScoreCommand(gameManager, "score"));
        addSubCommand(new PresetCommand(plugin, gameManager, "preset"));
    }
    
    @Override
    protected @NotNull Usage getSubCommandUsageArg(Permissible permissible) {
        return new Usage("<options>");
    }
}
