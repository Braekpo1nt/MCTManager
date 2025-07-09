package org.braekpo1nt.mctmanager.games.game.footrace.editor.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.game.footrace.config.FootRaceConfig;
import org.braekpo1nt.mctmanager.games.game.footrace.editor.FootRaceAdmin;
import org.braekpo1nt.mctmanager.games.game.footrace.editor.FootRaceEditor;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class FootRaceEditorStateBase implements FootRaceEditorState {
    
    protected final @NotNull FootRaceEditor context;
    
    public FootRaceEditorStateBase(@NotNull FootRaceEditor context) {
        this.context = context;
    }
    
    @Override
    public void cleanup() {
        
    }
    
    @Override
    public void onAdminJoin(FootRaceAdmin admin) {
        context.selectCheckpoint(
                admin,
                admin.getCurrentCheckpoint(),
                true
        );
    }
    
    @Override
    public void onAdminQuit(FootRaceAdmin admin) {
        
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
        FootRaceConfig config = context.getConfigController().getConfig(configFile);
        context.setConfig(config);
        return CommandResult.success(Component.empty()
                .append(Component.text("Loaded config from "))
                .append(Component.text(configFile)
                        .decorate(TextDecoration.ITALIC)));
    }
    
    @Override
    public void onAdminInteract(PlayerInteractEvent event, FootRaceAdmin admin) {
        
    }
    
    @Override
    public void onAdminDropItem(PlayerDropItemEvent event, FootRaceAdmin admin) {
        
    }
}
