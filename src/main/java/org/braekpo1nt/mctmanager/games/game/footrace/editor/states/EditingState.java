package org.braekpo1nt.mctmanager.games.game.footrace.editor.states;

import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.display.Renderer;
import org.braekpo1nt.mctmanager.games.game.footrace.editor.CheckpointRenderer;
import org.braekpo1nt.mctmanager.games.game.footrace.editor.FootRaceAdmin;
import org.braekpo1nt.mctmanager.games.game.footrace.editor.FootRaceEditor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EditingState extends FootRaceEditorStateBase {
    
    public EditingState(@NotNull FootRaceEditor context) {
        super(context);
        for (FootRaceAdmin admin : context.getAdmins().values()) {
            context.selectCheckpoint(
                    admin,
                    admin.getCurrentCheckpoint(),
                    true
            );
        }
    }
    
    @Override
    public @NotNull CommandResult loadConfig(@NotNull String configFile) throws ConfigException {
        CommandResult result = super.loadConfig(configFile);
        context.getStartingLocationRenderer().getRenderer().setLocation(context.getConfig().getStartingLocation());
        context.getStartingLocationRenderer().updateTitleLocation();
        context.getGlassBarrierRenderer().getRenderer().setBoundingBox(context.getConfig().getGlassBarrier());
        context.getGlassBarrierRenderer().updateTitleLocation();
        List<CheckpointRenderer> checkpointRenderers = context.getCheckpointRenderers();
        checkpointRenderers.forEach(Renderer::hide);
        checkpointRenderers.clear();
        checkpointRenderers.addAll(context.createCheckpointRenderers(context.getConfig()));
        checkpointRenderers.forEach(Renderer::show);
        for (FootRaceAdmin admin : context.getAdmins().values()) {
            int checkpointIndex = admin.getCurrentCheckpoint() < context.getConfig().getCheckpoints().size() ? admin.getCurrentCheckpoint() : 0;
            CommandResult selectResult = context.selectCheckpoint(
                    admin,
                    checkpointIndex,
                    false
            );
            admin.sendMessage(selectResult.getMessageOrEmpty());
        }
        return result;
    }
}
