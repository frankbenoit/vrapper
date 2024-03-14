package net.sourceforge.vrapper.vim.register;

import java.util.Set;

import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SelectionArea;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.SubstitutionOperation;
import net.sourceforge.vrapper.vim.commands.motions.FindCharMotion;
import net.sourceforge.vrapper.vim.commands.motions.NavigatingMotion;

/**
 * Provides access to different registers.
 *
 * @author Matthias Radig
 */
public interface RegisterManager {

    public static final String REGISTER_NAME_UNNAMED   = "\"";
    public static final String REGISTER_NAME_INSERT    = ".";
    /** System clipboard register. */
    public static final String REGISTER_NAME_CLIPBOARD = "+";
    /** "Last selection" clipboard register. */
    public static final String REGISTER_NAME_SELECTION = "*";
    public static final String REGISTER_NAME_SEARCH    = "/";
    public static final String REGISTER_SMALL_DELETE   = "-";
    public static final String REGISTER_NAME_BLACKHOLE = "_";
    /** Name of last executed macro. */
    public static final String REGISTER_NAME_LAST      = "@";
    public static final String REGISTER_NAME_COMMAND   = ":";
    /** Absolute path of the current file, if available. */
    public static final String REGISTER_NAME_CURRENT_FILE = "%";
    public static final String CLIPBOARD_VALUE_UNNAMED = "unnamed";
    public static final String CLIPBOARD_VALUE_UNNAMEDPLUS = "unnamedplus";
    public static final String CLIPBOARD_VALUE_AUTOSELECT = "autoselect";
    public static final String CLIPBOARD_VALUE_AUTOSELECTPLUS = "autoselectplus";

    Set<String> getRegisterNames();
    Register getRegister(String name);
    Register getDefaultRegister();
    Register getActiveRegister();
    Register getLastEditRegister();
    void setLastNamedRegister(Register register);
    void setActiveRegister(String name);
    void setActiveRegister(Register register);
    void activateDefaultRegister();
    void activateLastEditRegister();
    Command getLastEdit();
    void setLastEdit(Command edit);
    void setLastInsertion(Command command);
    Command getLastInsertion();
    void setLastSubstitution(SubstitutionOperation operation);
    SubstitutionOperation getLastSubstitution();
    void setLastYank(RegisterContent register);
    void setLastDelete(RegisterContent register);
    void setLastFindCharMotion(FindCharMotion motion);
    FindCharMotion getLastFindCharMotion();
    /** Used to differentiate between f/t used in dfx vs plain fx. */
    void setLastNavigatingMotion(NavigatingMotion motion);
    /** Used to differentiate between f/t used in dfx vs plain fx. */
    NavigatingMotion getLastNavigatingMotion();
    Search getSearch();
    void setSearch(Search search);
    boolean isDefaultRegisterActive();
    void setLastActiveSelection(SelectionArea selectionArea);
    SelectionArea getLastActiveSelectionArea();
    public void setCurrentWorkingDirectory(String cwd);
    public String getCurrentWorkingDirectory();
    public void setLastCommand(String macroString);
}
