package org.braekpo1nt.mctmanager.commands.mct.hub;

import org.braekpo1nt.mctmanager.commands.manager.SubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HubCommand extends SubCommand {
    
    private final GameManager gameManager;
    
    public HubCommand(@NotNull GameManager gameManager, @NotNull String name) {
        super(name);
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 0) {
            return CommandResult.failure(getUsage());
        }
        if (!(sender instanceof Player player)) {
            return CommandResult.failure("This command can only be run by a player");
        }
        Participant participant = gameManager.getOnlineParticipant(player.getUniqueId());
        if (participant != null) {
            return gameManager.returnParticipantToHub(participant.getUniqueId());
        }
        return CommandResult.failure("This command can only be run by a participant");
    }
}
