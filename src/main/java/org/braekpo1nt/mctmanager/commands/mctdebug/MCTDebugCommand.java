package org.braekpo1nt.mctmanager.commands.mctdebug;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.ClassPicker;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.Loadout;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A utility command for testing various things, so I don't have to create a new command. 
 */
public class MCTDebugCommand implements TabExecutor, Listener {
    
    private final Main plugin;
    private final GameManager gameManager;
    
    public MCTDebugCommand(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        Objects.requireNonNull(this.plugin.getCommand("mctdebug")).setExecutor(this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Must be a player to run this command")
                    .color(NamedTextColor.RED));
            return true;
        }
        
        if (args.length != 0) {
            sender.sendMessage(Component.text("Usage: /mctdebug <arg> [options]")
                    .color(NamedTextColor.RED));
            return true;
        }
        
        Collection<Participant> onlineParticipants = gameManager.getOnlineParticipants();
        Map<String, Loadout> loadoutMap = new HashMap<>();
        loadoutMap.put("KNIGHT", new Loadout(
                Component.empty()
                        .append(Component.text("Knight")),
                new ItemStack(Material.STONE_SWORD),
                new ItemStack[] {new ItemStack(Material.STONE_SWORD)})
        );
        loadoutMap.put("ARCHER", new Loadout(
                Component.empty()
                        .append(Component.text("Archer")),
                new ItemStack(Material.BOW),
                new ItemStack[] {new ItemStack(Material.BOW)})
        );
        loadoutMap.put("ASSASSIN", new Loadout(
                Component.empty()
                        .append(Component.text("Assassin")),
                new ItemStack(Material.IRON_SWORD),
                new ItemStack[] {new ItemStack(Material.IRON_SWORD)})
        );
        ClassPicker classPicker = new ClassPicker(plugin, onlineParticipants, Color.WHITE, loadoutMap);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> classPicker.stop(false), 60*20L);
        
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
