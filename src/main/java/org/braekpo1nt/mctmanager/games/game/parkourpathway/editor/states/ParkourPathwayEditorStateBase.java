package org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.config.ParkourPathwayConfig;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.ParkourAdmin;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.ParkourPathwayEditor;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class ParkourPathwayEditorStateBase implements ParkourPathwayEditorState {
    
    protected final @NotNull ParkourPathwayEditor context;
    
    public ParkourPathwayEditorStateBase(@NotNull ParkourPathwayEditor context) {
        this.context = context;
    }
    
    @Override
    public void cleanup() {
        
    }
    
    @Override
    public void onAdminJoin(ParkourAdmin admin) {
        context.selectPuzzle(
                admin,
                admin.getCurrentPuzzle(),
                admin.getCurrentInBound(),
                admin.getCurrentCheckPoint(),
                true
        );
    }
    
    @Override
    public void onAdminQuit(ParkourAdmin admin) {
        
    }
    
    @Override
    public @NotNull CommandResult validateConfig(@NotNull String configFile) throws ConfigException {
        context.getConfigController().validateConfig(context.getConfig(), configFile);
        return CommandResult.success(Component.empty()
                .append(Component.text("Config is valid ("))
                .append(Component.text(configFile)
                        .decorate(TextDecoration.ITALIC))
                .append(Component.text(")")));
    }
    
    @Override
    public @NotNull CommandResult saveConfig(@NotNull String configFile) throws ConfigException {
        context.getConfigController().saveConfig(context.getConfig(), configFile);
        return CommandResult.success(Component.empty()
                .append(Component.text("Saved config to "))
                .append(Component.text(configFile)
                        .decorate(TextDecoration.ITALIC)));
    }
    
    @Override
    public @NotNull CommandResult loadConfig(@NotNull String configFile) throws ConfigException {
        ParkourPathwayConfig config = context.getConfigController().getConfig(configFile);
        context.setConfig(config);
        return CommandResult.success(Component.empty()
                .append(Component.text("Loaded config from "))
                .append(Component.text(configFile)
                        .decorate(TextDecoration.ITALIC)));
    }
    
    @Override
    public void onAdminInteract(PlayerInteractEvent event, ParkourAdmin admin) {
        
    }
    
    @Override
    public void onAdminDropItem(PlayerDropItemEvent event, ParkourAdmin admin) {
        
    }
}
