/*
 * @(#)SearchCommand.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.util.Command;


/**
   The SearchCommand builds a SearchReplaceFrame
   in the SEARCHMODE.
 */
public class SearchCommand extends Command {
    // private DrawApplication application;
    private SearchReplaceFrame searchFrame;

    public SearchCommand(String name) {
        super(name);
        // this.application = application;
        searchFrame = new SearchReplaceFrame(SearchReplaceFrame.SEARCHMODE);
    }

    public void execute() {
        searchFrame.reset();
        searchFrame.setVisible(true);
    }

    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        return true;
    }
}