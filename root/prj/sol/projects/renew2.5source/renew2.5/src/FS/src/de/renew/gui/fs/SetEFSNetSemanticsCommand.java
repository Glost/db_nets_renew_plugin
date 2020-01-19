/*
 * @(#)SetEFSNetSemanticsCommand.java 5.1
 *
 */
package de.renew.gui.fs;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.framework.DrawingEditor;

import CH.ifa.draw.standard.NullDrawingEditor;

import CH.ifa.draw.util.Command;

import de.renew.formalism.efsnet.SingleEFSNetCompiler;


/**
 * A command to switch the semantics in EFSNets between
 * "Value" and "Reference".
 */
public class SetEFSNetSemanticsCommand extends Command {
    private boolean fValueSem;

    /**
     * Constructs a net semantic switch command.
     * @param name the command name
     * @param valueSem whether value semantics are requested.
     */
    public SetEFSNetSemanticsCommand(String name, boolean valueSem) {
        super(name);
        fValueSem = valueSem;
    }

    protected DrawingEditor getEditor() {
        DrawPlugin plugin = DrawPlugin.getCurrent();
        return (plugin == null) ? NullDrawingEditor.INSTANCE
                                : plugin.getDrawingEditor();
    }

    public void execute() {
        SingleEFSNetCompiler.valueSem = fValueSem;
    }
}