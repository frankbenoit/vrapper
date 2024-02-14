package net.sourceforge.vrapper.vim.modes;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.StateUtils.union;

import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;

/**
 * Temporary visual line-wise mode to execute a single command on a selection.
 * The mode is to be switched to from the insert mode via <code>&lt;C-O&gt;V</code>.
 */
public class TempLinewiseVisualMode extends LinewiseVisualMode implements TemporaryMode {

    public static final String NAME = "temporary linewise visual mode";
    public static final String DISPLAY_NAME = "(insert) VISUAL LINE";

    public TempLinewiseVisualMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    protected State<Command> buildInitialState() {
        State<Command> switchTempModes = state(
                );
        return union(switchTempModes, super.buildInitialState());
    }
}
