package de.renew.gui.fs;

import CH.ifa.draw.util.CommandMenu;


/**
 * This class configures the Renew user interface when the EFS Net Compiler is
 * chosen.
 * <p>
 * </p>
 * Created: Fri Aug 12  2005
 * @author Michael Duvigneau
 **/
public class EFSGuiConfigurator extends FSGuiConfigurator {
    protected static final String MENU_ID_EFSSEMANTICS = "de.renew.gui.fs.efssemantics";

    /**
     * Registers formalism-specific entries in the attributes menu.
     **/
    protected void addFSOptions(CommandMenu optionsMenu) {
        super.addFSOptions(optionsMenu);
        CommandMenu semanticsMenu = new CommandMenu("Semantics");
        semanticsMenu.add(new SetEFSNetSemanticsCommand("Reference", false));
        semanticsMenu.add(new SetEFSNetSemanticsCommand("Value", true));
        optionsMenu.add(semanticsMenu);
    }
}