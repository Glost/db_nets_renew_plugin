package de.renew.refactoring.inline;

import CH.ifa.draw.framework.UndoableCommand;

import de.renew.gui.GuiPlugin;


/**
 * Abstract command subclass that implements the inline listener interface. It
 * can be used as a base class for inline commands. The implementation of
 * {@link #isExecutable()} returns {@code false} if an inline operation is
 * active. Subclasses need to call {@link #setInline(InlineController)} when an
 * inline operation is started.
 *
 * @author 2mfriedr
 */
public abstract class SingleInlineUndoableCommand extends UndoableCommand
        implements InlineListener {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(SingleInlineUndoableCommand.class);
    private boolean _isInlineActive = false;

    public SingleInlineUndoableCommand(String name) {
        super(name);
    }

    protected void setInline(InlineController inline) {
        inline.addListener(this);
        _isInlineActive = true;
    }

    @Override
    public boolean isExecutable() {
        return !_isInlineActive;
    }

    @Override
    public void inlineFinished() {
        _isInlineActive = false;
        setIsExecutable(isExecutable());
        GuiPlugin.getCurrent().getGui().recheckMenus();
    }
}