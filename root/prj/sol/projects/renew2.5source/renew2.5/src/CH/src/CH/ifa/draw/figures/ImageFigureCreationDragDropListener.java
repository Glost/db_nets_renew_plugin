package CH.ifa.draw.figures;

import CH.ifa.draw.application.AbstractFileDragDropListener;
import CH.ifa.draw.application.DrawingViewFrame;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.io.ImageFileFilter;

import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;

import java.io.File;
import java.io.FileFilter;

import java.util.Vector;


public class ImageFigureCreationDragDropListener
        extends AbstractFileDragDropListener {
    private DrawingViewFrame drawingViewFrame;

    public ImageFigureCreationDragDropListener(DrawingViewFrame drawingViewFrame) {
        this.drawingViewFrame = drawingViewFrame;
    }

    @Override
    protected FileFilter getFileFilter() {
        return new ImageFileFilter();
    }

    @Override
    protected void handleFiles(File[] files, Point loc) {
        if (files.length >= 1) {
            Vector<Figure> ImagesToAdd = new Vector<Figure>();
            DrawingView view = this.drawingViewFrame.view();
            for (File file : files) {
                Image image = ImageFigureCreationTool.createImage(file
                                  .getAbsolutePath(), this.drawingViewFrame);

                ImageFigure imageFigure = new ImageFigure(image,
                                                          file.getAbsolutePath(),
                                                          loc);
                Rectangle displayBox = imageFigure.displayBox();
                imageFigure.moveBy(-(displayBox.width / 2),
                                   -(displayBox.height / 2));
                ImagesToAdd.add(imageFigure);
            }
            view.addAll(ImagesToAdd);
            view.checkDamage();
        }
    }
}