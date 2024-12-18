package org.braekpo1nt.mctmanager.participant;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Participant implements Audience {
    
    /**
     * The player object that this Participant represents
     */
    @EqualsAndHashCode.Include
    private final @NotNull Player player;
    /**
     * The teamId of the team this Participant belongs to
     */
    private final @NotNull String teamId;
    
    /**
     * @return the UUID of the player this Participant represents
     */
    public UUID getUniqueId() {
        return player.getUniqueId();
    }
    
    // Start: Audience Delegates
    @Override
    public void sendMessage(@NotNull Component message) {
        player.sendMessage(message);
    }
    
    @Override
    public void sendMessage(@NotNull Component message, ChatType.Bound boundChatType) {
        player.sendMessage(message, boundChatType);
    }
    
    @Override
    public void sendMessage(@NotNull SignedMessage signedMessage, ChatType.Bound boundChatType) {
        player.sendMessage(signedMessage, boundChatType);
    }
    
    @Override
    public void sendActionBar(@NotNull Component message) {
        player.sendActionBar(message);
    }
    
    @Override
    public void sendPlayerListHeaderAndFooter(@NotNull Component header, @NotNull Component footer) {
        player.sendPlayerListHeaderAndFooter(header, footer);
    }
    
    @Override
    public <T> void sendTitlePart(@NotNull TitlePart<T> part, @NotNull T value) {
        player.sendTitlePart(part, value);
    }
    
    @Override
    public void clearTitle() {
        player.clearTitle();
    }
    
    @Override
    public void showBossBar(@NotNull BossBar bar) {
        player.showBossBar(bar);
    }
    
    @Override
    public void hideBossBar(@NotNull BossBar bar) {
        player.hideBossBar(bar);
    }
    
    @Override
    public void playSound(@NotNull Sound sound) {
        player.playSound(sound);
    }
    
    @Override
    public void playSound(@NotNull Sound sound, double x, double y, double z) {
        player.playSound(sound, x, y, z);
    }
    
    @Override
    public void playSound(@NotNull Sound sound, Sound.Emitter emitter) {
        player.playSound(sound, emitter);
    }
    
    @Override
    public void stopSound(@NotNull SoundStop stop) {
        player.stopSound(stop);
    }
    
    @Override
    public void openBook(Book.Builder book) {
        player.openBook(book);
    }
    
    @Override
    public void openBook(@NotNull Book book) {
        player.openBook(book);
    }
    
    @Override
    public void sendResourcePacks(@NotNull ResourcePackRequest request) {
        player.sendResourcePacks(request);
    }
    
    @Override
    public void removeResourcePacks(@NotNull UUID id, @NotNull UUID @NotNull ... others) {
        player.removeResourcePacks(id, others);
    }
    
    @Override
    public void clearResourcePacks() {
        player.clearResourcePacks();
    }
    // End: Audience Delegates
}
