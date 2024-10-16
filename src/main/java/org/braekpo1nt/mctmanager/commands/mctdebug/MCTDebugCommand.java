package org.braekpo1nt.mctmanager.commands.mctdebug;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.ui.maps.CustomMapRenderer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

/**
 * A utility command for testing various things, so I don't have to create a new command. 
 */
public class MCTDebugCommand implements TabExecutor, Listener {
    
    private final Main plugin;
    
    public MCTDebugCommand(Main plugin) {
        this.plugin = plugin;
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
        
        if (args.length != 1) {
            sender.sendMessage(Component.text("Usage: /mctdebug <arg> [options]")
                    .color(NamedTextColor.RED));
            return true;
        }
        
        String filePath = args[0];
        
        
        ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
        MapMeta mapMeta = (MapMeta) mapItem.getItemMeta();
        
        MapView mapView = Bukkit.createMap(player.getWorld());
        // add a renderer
        CustomMapRenderer mapRenderer = CustomMapRenderer.fromFile(new File(filePath));
        if (mapRenderer == null) {
            sender.sendMessage(Component.text("Unable to create map image. Check the url.").color(NamedTextColor.RED));
            return true;
        }
        mapView.getRenderers().clear();
        mapView.addRenderer(mapRenderer);
        
        mapMeta.setMapView(mapView);
        mapItem.setItemMeta(mapMeta);
        
        player.getInventory().addItem(mapItem);
        
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
