package org.braekpo1nt.mctmanager.commands;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;

/**
 * A utility command for testing various things, so I don't have to create a new command. 
 */
public class MCTDebugCommand implements CommandExecutor {
    
    private final Main plugin;
    private final WorldBorder worldBorder;
    private Inventory gui;
    
    public MCTDebugCommand(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("mctdebug").setExecutor(this);
        MVWorldManager worldManager = Main.multiverseCore.getMVWorldManager();
        World mechaWorld = worldManager.getMVWorld("FT").getCBWorld();
        this.worldBorder = mechaWorld.getWorldBorder();
        gui = Bukkit.createInventory(null, 9, Component.text(ChatColor.AQUA+"Custom GUI"));
        new DebugClickEvent(plugin, gui);
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
        Player player = ((Player) sender).getPlayer();
        
        ItemStack suicide = new ItemStack(Material.TNT);
        ItemStack feed = new ItemStack(Material.BREAD);
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
    
        ItemMeta suicideMeta = suicide.getItemMeta();
        suicideMeta.displayName(Component.text("Suicide").color(NamedTextColor.RED));
        suicideMeta.lore(Collections.singletonList(Component.text("/kill")));
        suicide.setItemMeta(suicideMeta);
    
        ItemMeta feedMeta = feed.getItemMeta();
        feedMeta.displayName(Component.text("Feed").color(NamedTextColor.DARK_GREEN));
        feedMeta.lore(Collections.singletonList(Component.text("Hunger no more.")));
        feed.setItemMeta(feedMeta);
    
        ItemMeta swordMeta = sword.getItemMeta();
        swordMeta.displayName(Component.text("Sword").color(NamedTextColor.LIGHT_PURPLE));
        swordMeta.lore(Collections.singletonList(Component.text("Get a sword.")));
        sword.setItemMeta(swordMeta);
        
        ItemStack[] menuItems = {suicide, feed, sword};
        gui.setContents(menuItems);
        player.openInventory(gui);
    
        
//        Component mainTitle = Component.text("Main title");
//        Component subTitle = Component.text("Subtitle");
//
//        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500));
//        Title title = Title.title(mainTitle, subTitle, times);
//        sender.showTitle(title);
        return true;
    }
    
    
    
    
    
}
