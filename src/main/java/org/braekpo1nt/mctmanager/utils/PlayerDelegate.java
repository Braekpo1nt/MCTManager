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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface PlayerDelegate extends Player {
    
    @NotNull Player getPlayer();
    
    @Override
    default void sendMessage(@NotNull Component message) {getPlayer().sendMessage(message);}
    
    default void sendMessage(@NotNull Component message, ChatType.Bound boundChatType) {
        getPlayer().sendMessage(message, boundChatType);
    }
    
    @Override
    default void sendMessage(@NotNull SignedMessage signedMessage, ChatType.Bound boundChatType) {
        getPlayer().sendMessage(signedMessage, boundChatType);
    }
    
    @Override
    default void sendActionBar(@NotNull Component message) {
        getPlayer().sendActionBar(message);
    }
    
    @Override
    default void sendPlayerListHeaderAndFooter(@NotNull Component header, @NotNull Component footer) {
        getPlayer().sendPlayerListHeaderAndFooter(header, footer);
    }
    
    @Override
    default <T> void sendTitlePart(@NotNull TitlePart<T> part, @NotNull T value) {
        getPlayer().sendTitlePart(part, value);
    }
    
    @Override
    default void clearTitle() {
        getPlayer().clearTitle();
    }
    
    @Override
    default void showBossBar(@NotNull BossBar bar) {
        getPlayer().showBossBar(bar);
    }
    
    @Override
    default void hideBossBar(@NotNull BossBar bar) {
        getPlayer().hideBossBar(bar);
    }
    
    @Override
    default void playSound(@NotNull Sound sound) {
        getPlayer().playSound(sound);
    }
    
    @Override
    default void playSound(@NotNull Sound sound, double x, double y, double z) {
        getPlayer().playSound(sound, x, y, z);
    }
    
    @Override
    default void playSound(@NotNull Sound sound, Sound.Emitter emitter) {
        getPlayer().playSound(sound, emitter);
    }
    
    @Override
    default void stopSound(@NotNull SoundStop stop) {
        getPlayer().stopSound(stop);
    }
    
    @Override
    default void openBook(Book.Builder book) {
        getPlayer().openBook(book);
    }
    
    @Override
    default void openBook(@NotNull Book book) {
        getPlayer().openBook(book);
    }
    
    @Override
    default void sendResourcePacks(@NotNull ResourcePackRequest request) {
        getPlayer().sendResourcePacks(request);
    }
    
    @Override
    default void removeResourcePacks(@NotNull UUID id, @NotNull UUID @NotNull ... others) {
        getPlayer().removeResourcePacks(id, others);
    }
    
    @Override
    default void clearResourcePacks() {
        getPlayer().clearResourcePacks();
    }
    // End: Audience Delegates
    
    @Override
    @NotNull
    default Audience filterAudience(@NotNull Predicate<? super Audience> filter) {
        return getPlayer().filterAudience(filter);
    }
    
    @Override
    default void forEachAudience(@NotNull Consumer<? super Audience> action) {
        getPlayer().forEachAudience(action);
    }
    
    @Override
    default void sendMessage(@NotNull ComponentLike message) {
        getPlayer().sendMessage(message);
    }
    
    @Override
    default void sendMessage(@NotNull ComponentLike message, ChatType.Bound boundChatType) {
        getPlayer().sendMessage(message, boundChatType);
    }
    
    @Override
    default void deleteMessage(@NotNull SignedMessage signedMessage) {
        getPlayer().deleteMessage(signedMessage);
    }
    
    @Override
    default void deleteMessage(SignedMessage.Signature signature) {
        getPlayer().deleteMessage(signature);
    }
    
    @Override
    default void sendActionBar(@NotNull ComponentLike message) {
        getPlayer().sendActionBar(message);
    }
    
    @Override
    default void sendPlayerListHeader(@NotNull ComponentLike header) {
        getPlayer().sendPlayerListHeader(header);
    }
    
    @Override
    default void sendPlayerListHeader(@NotNull Component header) {
        getPlayer().sendPlayerListHeader(header);
    }
    
    @Override
    default void sendPlayerListFooter(@NotNull ComponentLike footer) {
        getPlayer().sendPlayerListFooter(footer);
    }
    
    @Override
    default void sendPlayerListFooter(@NotNull Component footer) {
        getPlayer().sendPlayerListFooter(footer);
    }
    
    @Override
    default void sendPlayerListHeaderAndFooter(@NotNull ComponentLike header, @NotNull ComponentLike footer) {
        getPlayer().sendPlayerListHeaderAndFooter(header, footer);
    }
    
    @Override
    default void showTitle(@NotNull Title title) {
        getPlayer().showTitle(title);
    }
    
    @Override
    default void resetTitle() {
        getPlayer().resetTitle();
    }
    
    @Override
    default void stopSound(@NotNull Sound sound) {
        getPlayer().stopSound(sound);
    }
    
    @Override
    default void sendResourcePacks(@NotNull ResourcePackInfoLike first, @NotNull ResourcePackInfoLike... others) {
        getPlayer().sendResourcePacks(first, others);
    }
    
    @Override
    default void sendResourcePacks(@NotNull ResourcePackRequestLike request) {
        getPlayer().sendResourcePacks(request);
    }
    
    @Override
    default void removeResourcePacks(@NotNull ResourcePackRequestLike request) {
        getPlayer().removeResourcePacks(request);
    }
    
    @Override
    default void removeResourcePacks(@NotNull ResourcePackRequest request) {
        getPlayer().removeResourcePacks(request);
    }
    
    @Override
    default void removeResourcePacks(@NotNull ResourcePackInfoLike request, @NotNull ResourcePackInfoLike @NotNull ... others) {
        getPlayer().removeResourcePacks(request, others);
    }
    
    @Override
    default void removeResourcePacks(@NotNull Iterable<UUID> ids) {
        getPlayer().removeResourcePacks(ids);
    }
    
    @Override
    default @NotNull <T> Optional<T> get(@NotNull Pointer<T> pointer) {
        return getPlayer().get(pointer);
    }
    
    @Override
    @Contract("_, null -> _; _, !null -> !null")
    default <T> @Nullable T getOrDefault(@NotNull Pointer<T> pointer, @Nullable T defaultValue) {
        return getPlayer().getOrDefault(pointer, defaultValue);
    }
    
    @Override
    default <T> @UnknownNullability T getOrDefaultFrom(@NotNull Pointer<T> pointer, @NotNull Supplier<? extends T> defaultValue) {
        return getPlayer().getOrDefaultFrom(pointer, defaultValue);
    }
    
    @Override
    default @NotNull Pointers pointers() {
        return getPlayer().pointers();
    }
}
