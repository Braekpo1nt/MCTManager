package org.braekpo1nt.mctmanager.games.editor;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CompositeCommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.editor.states.EditorStateBase;
import org.braekpo1nt.mctmanager.games.editor.wand.Wand;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.GameEditor;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

@Getter
@Setter
public abstract class EditorBase<A extends Admin, S extends EditorStateBase<A>> implements GameEditor, Listener {
    
    protected final @NotNull GameType type;
    protected final @NotNull Main plugin;
    protected final @NotNull GameManager gameManager;
    protected final @NotNull Sidebar sidebar;
    protected final @NotNull Map<UUID, A> admins;
    protected final @NotNull Component title;
    protected final @NotNull Collection<Wand<A>> wands;
    
    protected @NotNull String configFile;
    protected @NotNull S state;
    private int wandTickTaskId;
    
    /**
     * 
     * @param type the type associated with this editor
     * @param plugin the plugin
     * @param gameManager the GameManager
     * @param initialState the initialization state, should not contain any editor functionality.
     *                     The state must never be null, so this is what the state should be
     *                     as the game is being initialized to prevent null-pointer
     *                     exceptions. 
     */
    public EditorBase(
            @NotNull GameType type,
            @NotNull String configFile,
            @NotNull Main plugin, 
            @NotNull GameManager gameManager,
            @NotNull S initialState) {
        this.type = type;
        this.configFile = configFile;
        this.title = Component.empty()
                .append(Component.text("Editing: "))
                .append(Component.text(type.getTitle()));
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.sidebar = gameManager.createSidebar();
        this.admins = new HashMap<>();
        this.wands = new ArrayList<>(2);
        this.state = initialState;
    }
    
    protected @NotNull List<Wand<A>> defaultWands() {
        return List.of(
                new Wand<A>(Material.LIME_DYE, "Save config", List.of(
                        Component.text("Left Click: Validate the config"),
                        Component.text("Right Click: Save the config"),
                        Component.text("- (Crouch to skip validation)")
                ))
                        .onLeftClickAir((event, admin) -> configIsValid(configFile))
                        .onRightClickAir((event, admin) -> saveConfig(configFile, false))
                        .onRightSneakClickAir((event, admin) -> saveConfig(configFile, true)),
                new Wand<A>(Material.RED_DYE, "Load config", List.of(
                        Component.text("Right Click: Load the config")
                ))
                        .onRightClickAir((event, admin) -> loadConfig(configFile))
        );
    }
    
    protected @NotNull Wand<A> addWand(@NotNull Wand<A> wand) {
        wands.add(wand);
        return wand;
    }
    
    /**
     * Kicks off a task in which every {@link Wand} in {@link #wands}'s 
     * {@link Wand#onHoldTick(PlayerInventory, Audience)} method is called for every {@link A} in {@link #admins}
     */
    protected void startWandTick() {
        this.wandTickTaskId = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            admins.values().forEach(admin -> wands.forEach(wand -> wand.onHoldTick(admin.getPlayer().getInventory(), admin)));
        }, 0L, 10L).getTaskId();
    }
    
    /**
     * <p>Call this after all fields have been initialized. 
     * This initializes all admins, 
     * and finally assigns {@link #getStartState()} to {@link #state}.</p>
     * @param newAdmins the admins going into the editor
     */
    protected void start(@NotNull Collection<Player> newAdmins) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        wands.addAll(defaultWands());
        for (Player newAdmin : newAdmins) {
            A admin = createAdmin(newAdmin);
            admin.getPlayer().setGameMode(GameMode.CREATIVE);
            ParticipantInitializer.clearStatusEffects(newAdmin);
            ParticipantInitializer.clearInventory(newAdmin);
            ParticipantInitializer.resetHealthAndHunger(newAdmin);
            addAdmin(admin);
            initializeAdmin(admin);
        }
        _initializeSidebar();
        startWandTick();
        this.state = getStartState();
    }
    
    /**
     * <p>This will be assigned to {@link #state} at the end of 
     * {@link #start(Collection)}. This state should kick off the editor loop.</p>
     * @return the state to be instantiated after initialization
     */
    protected abstract @NotNull S getStartState();
    
    public void setState(@NotNull S state) {
        this.state = state;
    }
    
    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        plugin.getServer().getScheduler().cancelTask(wandTickTaskId);
        state.cleanup();
        for (A admin : admins.values()) {
            _resetAdmin(admin);
        }
        sidebar.deleteAllLines();
        cleanup();
        gameManager.editorIsOver(admins.values().stream().map(Admin::getPlayer).toList());
        admins.clear();
        Main.logger().info("Stopping editor for " + type.getTitle());
    }
    
    /**
     * <p>Cleanup tasks for the end of the game</p>
     */
    protected abstract void cleanup();
    
    protected void _initializeSidebar() {
        sidebar.addLines(
                new KeyLine("title", title)
        );
        initializeSidebar();
    }
    
    protected abstract void initializeSidebar();
    
    protected void _resetAdmin(A admin) {
        ParticipantInitializer.clearStatusEffects(admin.getPlayer());
        ParticipantInitializer.clearInventory(admin.getPlayer());
        ParticipantInitializer.resetHealthAndHunger(admin.getPlayer());
        sidebar.removePlayer(admin.getPlayer());
        resetAdmin(admin);
    }
    
    /**
     * <p>Reset the admin</p>
     * @param admin the admin to reset
     */
    protected abstract void resetAdmin(A admin);
    
    protected abstract void initializeAdmin(A admin);
    
    @Override
    public void onAdminJoin(Player newAdmin) {
        ParticipantInitializer.clearStatusEffects(newAdmin);
        ParticipantInitializer.clearInventory(newAdmin);
        ParticipantInitializer.resetHealthAndHunger(newAdmin);
        A admin = createAdmin(newAdmin);
        addAdmin(admin);
        state.onAdminJoin(admin);
        sidebar.updateLine(admin.getUniqueId(), "title", title);
    }
    
    /**
     * <p>Add the admin to the editor, and to UI managers</p>
     * @param admin the admin to add
     */
    protected void addAdmin(A admin) {
        admins.put(admin.getUniqueId(), admin);
        sidebar.addPlayer(admin.getPlayer());
        admin.getPlayer().getInventory().addItem(wands.stream().map(Wand::getWandItem).toArray(ItemStack[]::new));
    }
    
    /**
     * <p>Create an admin from the given {@link Player}.</p>
     * <p>Called after setting the admin to the defaults.
     * Add additional setup logic here for every time an admin is created.</p>
     *
     * @param admin the player from which to derive the {@link A} type admin
     * @return the created {@link A} admin
     */
    protected abstract @NotNull A createAdmin(Player admin);
    
    @Override
    public void onAdminQuit(UUID uuid) {
        A admin = admins.get(uuid);
        if (admin == null) {
            return;
        }
        state.onAdminQuit(admin);
        admins.remove(uuid);
        admin.getPlayer().setGameMode(GameMode.SPECTATOR);
        _resetAdmin(admin);
        if (admins.isEmpty()) {
            stop();
        }
    }
    
    @Override
    public @NotNull GameType getType() {
        return type;
    }
    
    @Override
    public CommandResult configIsValid(@NotNull String configFile) {
        CommandResult result;
        try {
            result = state.validateConfig(configFile);
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Error validating config for editor %s", type.getTitle()), e);
            return CommandResult.failure(Component.text("Config is not valid for ")
                    .append(Component.text(type.getTitle())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". See console for details:\n"))
                    .append(Component.text(e.getMessage())));
        }
        this.configFile = configFile;
        return result;
    }
    
    @Override
    public CommandResult saveConfig(@NotNull String configFile, boolean skipValidation) throws ConfigIOException, ConfigInvalidException {
        List<CommandResult> results = new ArrayList<>();
        if (!skipValidation) {
            try {
                state.validateConfig(configFile);
            } catch (ConfigException e) {
                Main.logger().log(Level.SEVERE, String.format("Error validating config for editor %s", type.getTitle()), e);
                return CommandResult.failure(Component.text("Config is not valid for ")
                        .append(Component.text(type.getTitle())
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(". See console for details:\n"))
                        .append(Component.text(e.getMessage())));
            }
            results.add(CommandResult.success(Component.text("Config is valid.")));
        } else {
            results.add(CommandResult.success(Component.text("Skipping validation")));
        }
        try {
            results.add(state.saveConfig(configFile));
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Error saving config for editor %s", type.getTitle()), e);
            return CommandResult.failure(Component.text("An error occurred while attempting to save the config for ")
                    .append(Component.text(type.getTitle())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". See console for details:\n"))
                    .append(Component.text(e.getMessage())));
        }
        this.configFile = configFile;
        return CompositeCommandResult.all(results);
    }
    
    @Override
    public CommandResult loadConfig(@NotNull String configFile) throws ConfigIOException, ConfigInvalidException {
        CommandResult result;
        try {
            result = state.loadConfig(configFile);
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Error loading config for editor %s", type.getTitle()), e);
            return CommandResult.failure(Component.text("Can't start ")
                    .append(Component.text(type.getTitle())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". Error loading config file. See console for details:\n"))
                    .append(Component.text(e.getMessage())));
        }
        this.configFile = configFile;
        return result;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        A admin = admins.get(event.getPlayer().getUniqueId());
        if (admin == null) {
            return;
        }
        wands.forEach(wand -> wand.onPlayerInteract(event, admin));
        state.onAdminInteract(event, admin);
    }
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        A admin = admins.get(event.getPlayer().getUniqueId());
        if (admin == null) {
            return;
        }
        boolean isWand = wands.stream().anyMatch(wand -> wand.isWandItem(event.getItemDrop().getItemStack()));
        if (isWand) {
            event.setCancelled(true);
            return;
        }
        state.onAdminDropItem(event, admin);
    }
}
