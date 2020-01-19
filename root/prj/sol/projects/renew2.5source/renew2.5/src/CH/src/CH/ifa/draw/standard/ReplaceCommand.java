/*
 * @(#)ReplaceCommand.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.util.Command;


/**
   The SearchCommand builds a SearchReplaceFrame
   in the SEARCHREPLACEMODE.
 */
public class ReplaceCommand extends Command {
    // private DrawApplication application;
    private SearchReplaceFrame replaceFrame;

    public ReplaceCommand(String name) {
        super(name);
        replaceFrame = new SearchReplaceFrame(SearchReplaceFrame.SEARCHREPLACEMODE);
    }

    public void execute() {
        replaceFrame.reset();
        replaceFrame.setVisible(true);
    }

    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        return true;
    }
}