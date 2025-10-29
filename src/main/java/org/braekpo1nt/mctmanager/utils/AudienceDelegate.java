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
public interface AudienceDelegate extends Audience {
    
    /**
     * The return value of this method is used as the delegate for all {@link Audience}
     * methods that need to be implemented.
     * @return the delegate whose methods should be used as implementations of the audience
     */
    @NotNull Audience getAudience();
    
    // Start: Audience Delegates
    @Override
    default void sendMessage(@NotNull Component message) {
        getAudience().sendMessage(message);
    }
    
    @Override
    default void sendMessage(@NotNull Component message, ChatType.Bound boundChatType) {
        getAudience().sendMessage(message, boundChatType);
    }
    
    @Override
    default void sendMessage(@NotNull SignedMessage signedMessage, ChatType.Bound boundChatType) {
        getAudience().sendMessage(signedMessage, boundChatType);
    }
    
    @Override
    default void sendActionBar(@NotNull Component message) {
        getAudience().sendActionBar(message);
    }
    
    @Override
    default void sendPlayerListHeaderAndFooter(@NotNull Component header, @NotNull Component footer) {
        getAudience().sendPlayerListHeaderAndFooter(header, footer);
    }
    
    @Override
    default <T> void sendTitlePart(@NotNull TitlePart<T> part, @NotNull T value) {
        getAudience().sendTitlePart(part, value);
    }
    
    @Override
    default void clearTitle() {
        getAudience().clearTitle();
    }
    
    @Override
    default void showBossBar(@NotNull BossBar bar) {
        getAudience().showBossBar(bar);
    }
    
    @Override
    default void hideBossBar(@NotNull BossBar bar) {
        getAudience().hideBossBar(bar);
    }
    
    @Override
    default void playSound(@NotNull Sound sound) {
        getAudience().playSound(sound);
    }
    
    @Override
    default void playSound(@NotNull Sound sound, double x, double y, double z) {
        getAudience().playSound(sound, x, y, z);
    }
    
    @Override
    default void playSound(@NotNull Sound sound, Sound.Emitter emitter) {
        getAudience().playSound(sound, emitter);
    }
    
    @Override
    default void stopSound(@NotNull SoundStop stop) {
        getAudience().stopSound(stop);
    }
    
    @Override
    default void openBook(Book.Builder book) {
        getAudience().openBook(book);
    }
    
    @Override
    default void openBook(@NotNull Book book) {
        getAudience().openBook(book);
    }
    
    @Override
    default void sendResourcePacks(@NotNull ResourcePackRequest request) {
        getAudience().sendResourcePacks(request);
    }
    
    @Override
    default void removeResourcePacks(@NotNull UUID id, @NotNull UUID @NotNull ... others) {
        getAudience().removeResourcePacks(id, others);
    }
    
    @Override
    default void clearResourcePacks() {
        getAudience().clearResourcePacks();
    }
    // End: Audience Delegates
    
    @Override
    @NotNull
    default Audience filterAudience(@NotNull Predicate<? super Audience> filter) {
        return getAudience().filterAudience(filter);
    }
    
    @Override
    default void forEachAudience(@NotNull Consumer<? super Audience> action) {
        getAudience().forEachAudience(action);
    }
    
    @Override
    default void sendMessage(@NotNull ComponentLike message) {
        getAudience().sendMessage(message);
    }
    
    @Override
    default void sendMessage(@NotNull ComponentLike message, ChatType.Bound boundChatType) {
        getAudience().sendMessage(message, boundChatType);
    }
    
    @Override
    default void deleteMessage(@NotNull SignedMessage signedMessage) {
        getAudience().deleteMessage(signedMessage);
    }
    
    @Override
    default void deleteMessage(SignedMessage.Signature signature) {
        getAudience().deleteMessage(signature);
    }
    
    @Override
    default void sendActionBar(@NotNull ComponentLike message) {
        getAudience().sendActionBar(message);
    }
    
    @Override
    default void sendPlayerListHeader(@NotNull ComponentLike header) {
        getAudience().sendPlayerListHeader(header);
    }
    
    @Override
    default void sendPlayerListHeader(@NotNull Component header) {
        getAudience().sendPlayerListHeader(header);
    }
    
    @Override
    default void sendPlayerListFooter(@NotNull ComponentLike footer) {
        getAudience().sendPlayerListFooter(footer);
    }
    
    @Override
    default void sendPlayerListFooter(@NotNull Component footer) {
        getAudience().sendPlayerListFooter(footer);
    }
    
    @Override
    default void sendPlayerListHeaderAndFooter(@NotNull ComponentLike header, @NotNull ComponentLike footer) {
        getAudience().sendPlayerListHeaderAndFooter(header, footer);
    }
    
    @Override
    default void showTitle(@NotNull Title title) {
        getAudience().showTitle(title);
    }
    
    @Override
    default void resetTitle() {
        getAudience().resetTitle();
    }
    
    @Override
    default void stopSound(@NotNull Sound sound) {
        getAudience().stopSound(sound);
    }
    
    @Override
    default void sendResourcePacks(@NotNull ResourcePackInfoLike first, @NotNull ResourcePackInfoLike... others) {
        getAudience().sendResourcePacks(first, others);
    }
    
    @Override
    default void sendResourcePacks(@NotNull ResourcePackRequestLike request) {
        getAudience().sendResourcePacks(request);
    }
    
    @Override
    default void removeResourcePacks(@NotNull ResourcePackRequestLike request) {
        getAudience().removeResourcePacks(request);
    }
    
    @Override
    default void removeResourcePacks(@NotNull ResourcePackRequest request) {
        getAudience().removeResourcePacks(request);
    }
    
    @Override
    default void removeResourcePacks(@NotNull ResourcePackInfoLike request, @NotNull ResourcePackInfoLike @NotNull ... others) {
        getAudience().removeResourcePacks(request, others);
    }
    
    @Override
    default void removeResourcePacks(@NotNull Iterable<UUID> ids) {
        getAudience().removeResourcePacks(ids);
    }
    
    @Override
    default @NotNull <T> Optional<T> get(@NotNull Pointer<T> pointer) {
        return getAudience().get(pointer);
    }
    
    @Override
    @Contract("_, null -> _; _, !null -> !null")
    default <T> @Nullable T getOrDefault(@NotNull Pointer<T> pointer, @Nullable T defaultValue) {
        return getAudience().getOrDefault(pointer, defaultValue);
    }
    
    @Override
    default <T> @UnknownNullability T getOrDefaultFrom(@NotNull Pointer<T> pointer, @NotNull Supplier<? extends T> defaultValue) {
        return getAudience().getOrDefaultFrom(pointer, defaultValue);
    }
    
    @Override
    default @NotNull Pointers pointers() {
        return getAudience().pointers();
    }
}
