package org.braekpo1nt.mctmanager.utils;

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
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Used for when a wrapper of an {@link Audience}-implementing class wants to use
 * that field as its delegate for {@link Audience}
 */
public abstract class AudienceDelegate implements Audience {
    
    /**
     * The return value of this method is used as the delegate for all {@link Audience}
     * methods that need to be implemented.
     * 
     * @return the delegate whose methods should be used as implementations of the audience
     */
    public abstract @NotNull Audience getAudience();
    
    // Start: Audience Delegates
    @Override
    public void sendMessage(@NotNull Component message) {
        getAudience().sendMessage(message);
    }
    
    @Override
    public void sendMessage(@NotNull Component message, ChatType.Bound boundChatType) {
        getAudience().sendMessage(message, boundChatType);
    }
    
    @Override
    public void sendMessage(@NotNull SignedMessage signedMessage, ChatType.Bound boundChatType) {
        getAudience().sendMessage(signedMessage, boundChatType);
    }
    
    @Override
    public void sendActionBar(@NotNull Component message) {
        getAudience().sendActionBar(message);
    }
    
    @Override
    public void sendPlayerListHeaderAndFooter(@NotNull Component header, @NotNull Component footer) {
        getAudience().sendPlayerListHeaderAndFooter(header, footer);
    }
    
    @Override
    public <T> void sendTitlePart(@NotNull TitlePart<T> part, @NotNull T value) {
        getAudience().sendTitlePart(part, value);
    }
    
    @Override
    public void clearTitle() {
        getAudience().clearTitle();
    }
    
    @Override
    public void showBossBar(@NotNull BossBar bar) {
        getAudience().showBossBar(bar);
    }
    
    @Override
    public void hideBossBar(@NotNull BossBar bar) {
        getAudience().hideBossBar(bar);
    }
    
    @Override
    public void playSound(@NotNull Sound sound) {
        getAudience().playSound(sound);
    }
    
    @Override
    public void playSound(@NotNull Sound sound, double x, double y, double z) {
        getAudience().playSound(sound, x, y, z);
    }
    
    @Override
    public void playSound(@NotNull Sound sound, Sound.Emitter emitter) {
        getAudience().playSound(sound, emitter);
    }
    
    @Override
    public void stopSound(@NotNull SoundStop stop) {
        getAudience().stopSound(stop);
    }
    
    @Override
    public void openBook(Book.Builder book) {
        getAudience().openBook(book);
    }
    
    @Override
    public void openBook(@NotNull Book book) {
        getAudience().openBook(book);
    }
    
    @Override
    public void sendResourcePacks(@NotNull ResourcePackRequest request) {
        getAudience().sendResourcePacks(request);
    }
    
    @Override
    public void removeResourcePacks(@NotNull UUID id, @NotNull UUID @NotNull ... others) {
        getAudience().removeResourcePacks(id, others);
    }
    
    @Override
    public void clearResourcePacks() {
        getAudience().clearResourcePacks();
    }
    // End: Audience Delegates
    
}
