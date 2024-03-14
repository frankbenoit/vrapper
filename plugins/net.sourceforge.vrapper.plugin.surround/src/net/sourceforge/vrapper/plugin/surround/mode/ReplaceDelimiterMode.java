package net.sourceforge.vrapper.plugin.surround.mode;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.plugin.surround.commands.DelimiterChangedListener;
import net.sourceforge.vrapper.plugin.surround.state.AbstractDynamicDelimiterHolder;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.DelimitedText;
import net.sourceforge.vrapper.vim.modes.AbstractVisualMode;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;
import net.sourceforge.vrapper.vim.modes.commandline.AbstractCommandLineMode;
import net.sourceforge.vrapper.vim.modes.commandline.AbstractCommandParser;

/**
 * This custom mode opens the mini-buffer to update a set of dynamic delimiters.
 * 
 * @author Bert Jacobs
 */
public class ReplaceDelimiterMode extends AbstractCommandLineMode {

    private DelimitedText toWrap;
    private AbstractDynamicDelimiterHolder replacement;
    private Command leaveCommand;
    private DelimiterChangedListener listener;

    public ReplaceDelimiterMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    public static void switchMode(EditorAdaptor vim, Command leaveCommand,
            DelimiterChangedListener listener, DelimitedText toWrap,
            AbstractDynamicDelimiterHolder dynamicDelimiter) throws CommandExecutionException {

        ModeSwitchHint[] hints;
        if (vim.getCurrentMode() instanceof AbstractVisualMode) {
            hints = new ModeSwitchHint[2];
            hints[1] = AbstractCommandLineMode.FROM_VISUAL;
        } else {
            hints = new ModeSwitchHint[1];
        }
        hints[0] = new DelimiterHint(leaveCommand, listener, toWrap,
                dynamicDelimiter);
        vim.changeMode(ReplaceDelimiterMode.class.getName(), hints);
    }

    static class DelimiterHint implements ModeSwitchHint {
        protected Command command;
        protected DelimiterChangedListener listener;
        protected DelimitedText toWrap;
        protected AbstractDynamicDelimiterHolder replacement;

        public DelimiterHint(Command command, DelimiterChangedListener listener,
                             DelimitedText toWrap, AbstractDynamicDelimiterHolder replacement) {
            this.command = command;
            this.listener = listener;
            this.toWrap = toWrap;
            this.replacement = replacement;
        }
    }

    @Override
    protected String getPrompt() {
        return replacement.getTemplate(editorAdaptor, toWrap);
    }

    @Override
    public void enterMode(ModeSwitchHint... args) throws CommandExecutionException {
        if (args.length > 0 && args[0] instanceof DelimiterHint) {
            DelimiterHint hint = (DelimiterHint) args[0];
            toWrap = hint.toWrap;
            replacement = hint.replacement;
            leaveCommand = hint.command;
            listener = hint.listener;
        } else {
            throw new CommandExecutionException("No DelimiterHint passed!");
        }
        super.enterMode(args);
    }

    @Override
    public String getDisplayName() {
        return "SURROUND (" + replacement.getDelimiterDisplayName() + ")";
    }

    @Override
    public String getName() {
        return ReplaceDelimiterMode.class.getName();
    }

    @Override
    public String resolveKeyMap(KeyStroke stroke) {
        return null;
    }

    @Override
    protected AbstractCommandParser createParser() {
        return new DelimiterParser(editorAdaptor, leaveCommand, listener, toWrap, replacement);
    }
}
