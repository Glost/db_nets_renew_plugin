package de.renew.refactoring.wizard;

import CH.ifa.draw.util.Command;

import de.renew.gui.GuiPlugin;


/**
 * Abstract command subclass that implements the wizard listener interface. It
 * can be used as a base class for wizard commands. The implementation of
 * {@link #isExecutable()} returns {@code false} if a wizard is active.
 * Subclasses need to call {@link #setWizard(Wizard)} when a wizard is started.
 *
 * @author 2mfriedr
 */
public abstract class SingleWizardCommand extends Command
        implements WizardListener {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(SingleWizardCommand.class);
    private boolean _isWizardActive;

    public SingleWizardCommand(String name) {
        super(name);
    }

    protected void setWizard(Wizard wizard) {
        wizard.addListener(this);
        _isWizardActive = true;
        setIsExecutable(isExecutable());
    }

    @Override
    public boolean isExecutable() {
        return !_isWizardActive;
    }

    @Override
    public void wizardFinished() {
        _isWizardActive = false;
        setIsExecutable(isExecutable());
        GuiPlugin.getCurrent().getGui().recheckMenus();
    }
}