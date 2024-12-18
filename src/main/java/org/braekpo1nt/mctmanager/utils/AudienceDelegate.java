package org.braekpo1nt.mctmanager.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.pointer.Pointer;
import net.kyori.adventure.pointer.Pointers;
import net.kyori.adventure.resource.ResourcePackInfoLike;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.resource.ResourcePackRequestLike;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.jetbrains.annotations.*;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * <p></p>Used for when a wrapper of an {@link Audience}-implementing class wants to use
 * that field as its delegate for {@link Audience}. You only need to implement
 * {@link #getAudience()}, have it return the desired delegate.</p>
 * 
 * <p>Note: Does not implement deprecated or static methods from {@link Audience}.</p>
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
    
    @Override
    @NotNull
    public Audience filterAudience(@NotNull Predicate<? super Audience> filter) {
        return getAudience().filterAudience(filter);
    }
    
    @Override
    public void forEachAudience(@NotNull Consumer<? super Audience> action) {
        getAudience().forEachAudience(action);
    }
    
    @Override
    public void sendMessage(@NotNull ComponentLike message) {
        getAudience().sendMessage(message);
    }
    
    @Override
    public void sendMessage(@NotNull ComponentLike message, ChatType.Bound boundChatType) {
        getAudience().sendMessage(message, boundChatType);
    }
    
    @Override
    public void deleteMessage(@NotNull SignedMessage signedMessage) {
        getAudience().deleteMessage(signedMessage);
    }
    
    @Override
    public void deleteMessage(SignedMessage.Signature signature) {
        getAudience().deleteMessage(signature);
    }
    
    @Override
    public void sendActionBar(@NotNull ComponentLike message) {
        getAudience().sendActionBar(message);
    }
    
    @Override
    public void sendPlayerListHeader(@NotNull ComponentLike header) {
        getAudience().sendPlayerListHeader(header);
    }
    
    @Override
    public void sendPlayerListHeader(@NotNull Component header) {
        getAudience().sendPlayerListHeader(header);
    }
    
    @Override
    public void sendPlayerListFooter(@NotNull ComponentLike footer) {
        getAudience().sendPlayerListFooter(footer);
    }
    
    @Override
    public void sendPlayerListFooter(@NotNull Component footer) {
        getAudience().sendPlayerListFooter(footer);
    }
    
    @Override
    public void sendPlayerListHeaderAndFooter(@NotNull ComponentLike header, @NotNull ComponentLike footer) {
        getAudience().sendPlayerListHeaderAndFooter(header, footer);
    }
    
    @Override
    public void showTitle(@NotNull Title title) {
        getAudience().showTitle(title);
    }
    
    @Override
    public void resetTitle() {
        getAudience().resetTitle();
    }
    
    @Override
    public void stopSound(@NotNull Sound sound) {
        getAudience().stopSound(sound);
    }
    
    @Override
    public void sendResourcePacks(@NotNull ResourcePackInfoLike first, @NotNull ResourcePackInfoLike... others) {
        getAudience().sendResourcePacks(first, others);
    }
    
    @Override
    public void sendResourcePacks(@NotNull ResourcePackRequestLike request) {
        getAudience().sendResourcePacks(request);
    }
    
    @Override
    public void removeResourcePacks(@NotNull ResourcePackRequestLike request) {
        getAudience().removeResourcePacks(request);
    }
    
    @Override
    public void removeResourcePacks(@NotNull ResourcePackRequest request) {
        getAudience().removeResourcePacks(request);
    }
    
    @Override
    public void removeResourcePacks(@NotNull ResourcePackInfoLike request, @NotNull ResourcePackInfoLike @NotNull ... others) {
        getAudience().removeResourcePacks(request, others);
    }
    
    @Override
    public void removeResourcePacks(@NotNull Iterable<UUID> ids) {
        getAudience().removeResourcePacks(ids);
    }
    
    @Override
    public @NotNull <T> Optional<T> get(@NotNull Pointer<T> pointer) {
        return getAudience().get(pointer);
    }
    
    @Override
    @Contract("_, null -> _; _, !null -> !null")
    public <T> @Nullable T getOrDefault(@NotNull Pointer<T> pointer, @Nullable T defaultValue) {
        return getAudience().getOrDefault(pointer, defaultValue);
    }
    
    @Override
    public <T> @UnknownNullability T getOrDefaultFrom(@NotNull Pointer<T> pointer, @NotNull Supplier<? extends T> defaultValue) {
        return getAudience().getOrDefaultFrom(pointer, defaultValue);
    }
    
    @Override
    public @NotNull Pointers pointers() {
        return getAudience().pointers();
    }
}
