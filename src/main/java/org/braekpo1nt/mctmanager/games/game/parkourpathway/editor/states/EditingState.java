package org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.states;

import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.display.Renderer;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.ParkourAdmin;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.ParkourPathwayEditor;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.editor.PuzzleRenderer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EditingState extends ParkourPathwayEditorStateBase {
    public EditingState(@NotNull ParkourPathwayEditor context) {
        super(context);
        for (ParkourAdmin admin : context.getAdmins().values()) {
            context.selectPuzzle(
                    admin,
                    admin.getCurrentPuzzle(),
                    admin.getCurrentInBound(),
                    admin.getCurrentCheckPoint(),
                    true);
        }
    }
    
    @Override
    public @NotNull CommandResult loadConfig(@NotNull String configFile) throws ConfigException {
        CommandResult result = super.loadConfig(configFile);
        context.setPuzzles(context.getConfig().getPuzzles());
        List<PuzzleRenderer> puzzleRenderers = context.getPuzzleRenderers();
        puzzleRenderers.forEach(Renderer::hide);
        puzzleRenderers.clear();
        puzzleRenderers.addAll(context.createPuzzleRenderers(context.getConfig()));
        puzzleRenderers.forEach(Renderer::show);
        for (ParkourAdmin admin : context.getAdmins().values()) {
            int puzzleIndex =
                    admin.getCurrentPuzzle() < context.getPuzzles().size() ?
                            admin.getCurrentPuzzle() : 0;
            CommandResult selectResult = context.selectPuzzle(
                    admin,
                    puzzleIndex,
                    0,
                    0,
                    false
            );
            admin.sendMessage(selectResult.getMessageOrEmpty());
        }
        return result;
    }
}
