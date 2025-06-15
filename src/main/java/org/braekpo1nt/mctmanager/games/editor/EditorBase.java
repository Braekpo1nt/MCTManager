package org.braekpo1nt.mctmanager.games.editor;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CompositeCommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.editor.states.EditorStateBase;
import org.braekpo1nt.mctmanager.games.editor.wand.SpecialWand;
import org.braekpo1nt.mctmanager.games.editor.wand.Wand;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.GameEditor;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
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
    protected final @NotNull Collection<Wand> wands;
    protected final @NotNull Collection<SpecialWand<A>> specialWands;
    
    protected @NotNull S state;
    
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
            @NotNull Main plugin, 
            @NotNull GameManager gameManager,
            @NotNull S initialState) {
        this.type = type;
        this.title = Component.empty()
                .append(Component.text("Editing: "))
                .append(Component.text(type.getTitle()));
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.sidebar = gameManager.createSidebar();
        this.admins = new HashMap<>();
        this.wands = new ArrayList<>();
        this.specialWands = new ArrayList<>();
        this.state = initialState;
    }
    
    /**
     * 
     * @param wand the {@link Wand} to add
     */
    protected void addWand(@NotNull Wand wand) {
        wands.add(wand);
    }
    
    protected @NotNull SpecialWand<A> addWand(@NotNull SpecialWand<A> specialWand) {
        specialWands.add(specialWand);
        return specialWand;
    }
    
    /**
     * <p>Call this after all fields have been initialized. 
     * This initializes all admins, 
     * and finally assigns {@link #getStartState()} to {@link #state}.</p>
     * @param newAdmins the admins going into the editor
     */
    protected void start(@NotNull Collection<Player> newAdmins) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
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
        admin.getPlayer().getInventory().addItem(specialWands.stream().map(SpecialWand::getWandItem).toArray(ItemStack[]::new));
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
    }
    
    @Override
    public @NotNull GameType getType() {
        return type;
    }
    
    @Override
    public CommandResult configIsValid(@NotNull String configFile) {
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
        return CommandResult.success(Component.text("Config is valid."));
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
            state.saveConfig(configFile);
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Error saving config for editor %s", type.getTitle()), e);
            return CommandResult.failure(Component.text("An error occurred while attempting to save the config for ")
                    .append(Component.text(type.getTitle())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". See console for details:\n"))
                    .append(Component.text(e.getMessage())));
        }
        results.add(CommandResult.success(Component.text("Config is saved.")));
        return CompositeCommandResult.all(results);
    }
    
    @Override
    public CommandResult loadConfig(@NotNull String configFile) throws ConfigIOException, ConfigInvalidException {
        try {
            state.loadConfig(configFile);
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Error loading config for editor %s", type.getTitle()), e);
            return CommandResult.failure(Component.text("Can't start ")
                    .append(Component.text(type.getTitle())
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". Error loading config file. See console for details:\n"))
                    .append(Component.text(e.getMessage())));
        }
        return CommandResult.success(Component.text("Config loaded."));
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        A admin = admins.get(event.getPlayer().getUniqueId());
        if (admin == null) {
            return;
        }
        for (Wand wand : wands) {
            wand.onPlayerInteract(event);
        }
        for (SpecialWand<A> specialWand : specialWands) {
            specialWand.onPlayerInteract(event, admin);
        }
        state.onAdminInteract(event, admin);
    }
}
