package org.braekpo1nt.mctmanager.commands.mct.team.teamChat;

import org.braekpo1nt.mctmanager.Main;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TeamChatCommand extends Command{
    Main plugin;
    List<Player> playersInTeam;
    StringBuffer strbffr = new StringBuffer();
    List<String> messageStrings;
    String message;
    public TeamChatCommand(@NotNull Main plugin, CommandExecutor commandExecutor){
        super("TeamChat");
        this.usageMessage = "";
        this.plugin = plugin;

        Objects.requireNonNull(plugin.getCommand("TeamChat")).setExecutor(commandExecutor);
    }

    @Override
    public boolean execute(CommandSender commandSender, String str, String[] args) {
        if(!(commandSender instanceof Player))return false;
        else {

            switch(args.length){
                case 1 -> {
                    //Fix color, unsure of where it is located. Better if you do it - Q
                    ((Player) commandSender).sendRawMessage("Include a message when using this command.");
                }
                default -> sendMessage(commandSender, args);
            }
            return true;
        }
    }

    public void sendMessage(CommandSender commandSender, String[] args){

        UUID uniqueUID = ((Player) commandSender).getUniqueId();
        messageStrings = List.of(Arrays.toString(args));

        /* index is 1, because of the skipped initial command which will be "/Teamchat" in "/Teamchat <message>" (if im not wrong lmao)*/
        int index = 1;
        for(String : messageStrings){
            if(index == messageStrings.size()){
                strbffr.append(messageStrings.get(index));
            }
            else{
                strbffr.append(messageStrings.get(index));
                strbffr.append(" ");
                index++;
            }
        }

        message = strbffr.toString();

        /*
         * Logic on how to locate team players via UUID.
         * Define as player objects.
         * tell-raw player objects with certain information from commandsender forwarded.
         * */

        messageStrings = null;
    }
}
