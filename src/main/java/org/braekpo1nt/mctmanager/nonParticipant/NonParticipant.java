package org.braekpo1nt.mctmanager.nonParticipant;

import io.papermc.paper.entity.LookAnchor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.utils.AudienceDelegate;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Collection;
import java.util.List;

@Getter
@ToString(callSuper = true)
public class NonParticipant extends OfflineNonParticipant implements AudienceDelegate {
    
    public static String toString(Collection<? extends NonParticipant> nonParticipants) {
        return nonParticipants.stream()
                .map(NonParticipant::getName)
                .toList()
                .toString();
    }
    
    public static <P extends NonParticipant> List<Player> toPlayersList(Collection<P> nonParticipants) {
        return nonParticipants.stream().map(NonParticipant::getPlayer).toList();
    }
    
    @EqualsAndHashCode.Include
    protected final @NotNull Player player;
    
    public NonParticipant(@NotNull NonParticipant nonParticipant) {
        super(nonParticipant);
        this.player = nonParticipant.getPlayer();
    }
    
    public NonParticipant(@NotNull NonParticipant nonParticipant, @NotNull String name, @NotNull Component displayName) {
        super(nonParticipant, name, displayName);
        this.player = nonParticipant.getPlayer();
    }
    
    public NonParticipant(@NotNull OfflineNonParticipant offlineNonParticipant, @NotNull Player player) {
        super(offlineNonParticipant);
        this.player = player;
    }
    
    public NonParticipant(@NotNull Player player) {
        super(player);
        this.player = player;
    }
    
    @Override
    public @NotNull Audience getAudience() {return player;}
    
    @Override
    public @NotNull Player getPlayer() {return player;}
    
    @Override
    public @NotNull String getName() {return player.getName();}
    
    public boolean teleport(@NotNull Location location) {return player.teleport(location);}
    
    public void setRespawnLocation(@Nullable Location location) {player.setRespawnLocation(location);}
    
    public void setRespawnLocation(@Nullable Location location, boolean force) {
        player.setRespawnLocation(location, force);
    }
    
    public void setGameMode(@NotNull GameMode mode) {player.setGameMode(mode);}
    
    public @Nullable InventoryView openInventory(@NotNull Inventory inventory) {
        return player.openInventory(inventory);
    }
    
    public void closeInventory() {player.closeInventory();}
    
    public @NotNull PlayerInventory getInventory() {return player.getInventory();}
    
    @Override
    public @NotNull Component displayName() {return player.displayName();}
    
    public void lookAt(double x, double y, double z, @NotNull LookAnchor lookAnchor) {
        player.lookAt(x, y, z, lookAnchor);
    }
    
    public @NotNull World getWorld() {return player.getWorld();}
    
    public @NotNull Location getLocation() {return player.getLocation();}
    
    public @NotNull BlockFace getFacing() {return player.getFacing();}
    
    public void playSound(@NotNull Location location, @NotNull String sound, float volume, float pitch) {
        player.playSound(location, sound, volume, pitch);
    }
    
    public void addPotionEffect(PotionEffect potionEffect) {
        player.addPotionEffect(potionEffect);
    }
    
    public void removePotionEffect(@NotNull PotionEffectType type) {
        player.removePotionEffect(type);
    }
    
    public void setFoodLevel(int value) {
        player.setFoodLevel(value);
    }
    
    public @NotNull GameMode getGameMode() {
        return player.getGameMode();
    }
    
    public int getLevel() {
        return player.getLevel();
    }
    
    public void sendMessage(@NotNull String message) {
        player.sendMessage(message);
    }
    
    public @NotNull InventoryView getOpenInventory() {
        return player.getOpenInventory();
    }
}
