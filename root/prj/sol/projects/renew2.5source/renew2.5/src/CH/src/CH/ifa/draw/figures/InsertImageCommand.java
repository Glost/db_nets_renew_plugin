/*
 * @(#)InsertImageCommand.java 5.1
 *
 */
package CH.ifa.draw.figures;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.UndoableCommand;

import CH.ifa.draw.util.Iconkit;

import java.awt.Component;
import java.awt.Image;


/**
 * Command to insert a named image.
 */
public class InsertImageCommand extends UndoableCommand {
    protected DrawingEditor fEditor;
    private String fImage;

    /**
     * Constructs an insert image command.
     * @param name the command name
     * @param image the pathname of the image
     */
    public InsertImageCommand(String name, String image) {
        super(name);
        fImage = image;
        // fEditor = editor;
    }

    public boolean executeUndoable() {
        DrawingView view = getEditor().view();

        // ugly cast to component, but AWT wants an Component instead of an ImageObserver...
        Image image = Iconkit.instance()
                             .registerAndLoadImage((Component) view, fImage);
        ImageFigure figure = new ImageFigure(image, fImage, view.lastClick());
        view.add(figure);
        view.clearSelection();
        view.addToSelection(figure);
        view.checkDamage();
        return true;
    }
}