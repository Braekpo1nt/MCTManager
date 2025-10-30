package org.braekpo1nt.mctmanager.commands.bugreport;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class BugReportCommand implements TabExecutor {
    
    private final GameManager gameManager;
    private final Main plugin;
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public BugReportCommand(Main plugin, GameManager gameManager) {
        PluginCommand command = plugin.getCommand("bugreport");
        this.gameManager = gameManager;
        this.plugin = plugin;
        if (command == null) {
            Main.logger().severe("Unable to find /bugreport command in plugin.yml");
            return;
        }
        command.setExecutor(this);
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
            return true;
        }
        
        Participant participant = gameManager.getOnlineParticipant(player.getUniqueId());
        if (participant == null) {
            sender.sendMessage(Component.text("Only participants can use this command", NamedTextColor.RED));
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /bugreport <message>", NamedTextColor.RED));
            return true;
        }
        
        String bugMessage = String.join(" ", args);
        
        try {
            logBugReport(participant, bugMessage);
            sender.sendMessage(Component.text("Bug report submitted. Thank you!", NamedTextColor.GREEN));
            
            // Notify admins
            Component adminNotification = Component.empty()
                    .append(Component.text("[BUG REPORT] ", NamedTextColor.RED))
                    .append(participant.displayName())
                    .append(Component.text(": ", NamedTextColor.WHITE))
                    .append(Component.text(bugMessage, NamedTextColor.GRAY));
            
            Audience.audience(gameManager.getOnlineAdmins()).sendMessage(adminNotification);
        } catch (IOException e) {
            Main.logger().log(Level.SEVERE, "Failed to write bug report", e);
            sender.sendMessage(Component.text("Failed to submit bug report. Please try again.", NamedTextColor.RED));
        }
        
        return true;
    }
    
    private void logBugReport(@NotNull Participant participant, String bugMessage) throws IOException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        File bugReportFile = new File(dataFolder, "bug-reports.csv");
        boolean fileExists = bugReportFile.exists();
        
        try (FileWriter writer = new FileWriter(bugReportFile, true)) {
            // Write header if file is new
            if (!fileExists) {
                writer.append("IGN,UUID,Timestamp,Current Game,Player World,Player X,Player Y,Player Z,Bug message\n");
            }
            
            String currentGame = getCurrentGameName(participant);
            @NotNull Location location = participant.getLocation();
            String world = location.getWorld().getName();
            double x = location.getX();
            double y = location.getY();
            double z = location.getZ();
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            
            // Escape CSV values
            String escapedName = escapeCsv(participant.getName());
            String escapedGame = escapeCsv(currentGame);
            String escapedMessage = escapeCsv(bugMessage);
            
            writer.append(escapedName).append(",")
                    .append(participant.getUniqueId().toString()).append(",")
                    .append(timestamp).append(",")
                    .append(escapedGame).append(",")
                    .append(world).append(",")
                    .append(String.valueOf(x)).append(",")
                    .append(String.valueOf(y)).append(",")
                    .append(String.valueOf(z)).append(",")
                    .append(escapedMessage).append("\n");
        }
    }
    
    private String getCurrentGameName(Participant participant) {
        GameInstanceId gameInstanceId = gameManager.getTeamActiveGame(participant.getTeamId());
        if (gameInstanceId == null) {
            return "";
        }
        return gameInstanceId.getTitle();
    }
    
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // If value contains comma, quote, or newline, wrap in quotes and escape quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}