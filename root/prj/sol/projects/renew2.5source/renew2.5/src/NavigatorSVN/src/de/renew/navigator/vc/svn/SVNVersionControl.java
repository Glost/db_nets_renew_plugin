package de.renew.navigator.vc.svn;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatusClient;
import org.tmatesoft.svn.core.wc.SVNWCClient;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.DrawingFileHelper;
import CH.ifa.draw.io.StatusDisplayer;
import CH.ifa.draw.io.StorableInputDrawingLoader;

import CH.ifa.draw.util.StorableInput;

import de.renew.imagenetdiff.PNGDiffCommand;

import de.renew.navigator.vc.AbstractVersionControl;
import de.renew.navigator.vc.Repository;
import de.renew.navigator.vc.StdoutStatusDisplayer;

import de.renew.svn.SVNUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2016-02-29
 */
public class SVNVersionControl extends AbstractVersionControl {
    private final SVNStatusClient statusClient;
    private final SVNWCClient wcClient;
    private final SVNUtil svnUtil;
    private final SVNLogClient logClient;

    public SVNVersionControl() {
        final SVNClientManager manager = SVNClientManager.newInstance();
        statusClient = manager.getStatusClient();
        wcClient = manager.getWCClient();
        logClient = manager.getLogClient();
        svnUtil = SVNUtil.getInstance();
    }

    public SVNStatusClient getStatusClient() {
        return statusClient;
    }

    public SVNWCClient getWcClient() {
        return wcClient;
    }

    public SVNLogClient getLogClient() {
        return logClient;
    }

    @Override
    public Repository buildRepository(File file) {
        return buildSVNRepository(file);
    }

    @Override
    public boolean diff(File file) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wcClient.doGetFileContents(file, SVNRevision.BASE,
                                       SVNRevision.BASE, false, baos);
            String bytes = baos.toString();
            if (logger.isDebugEnabled()) {
                logger.debug("SVN:  file path: " + file.getPath());
            }
            if (bytes == null) {
                if (logger.isInfoEnabled()) {
                    logger.info("SVN: There is no HEAD here. File possibly not in repository. Received null.");
                }
                return false;
            }

            final StatusDisplayer displayer = new StdoutStatusDisplayer();

            Drawing drawing = StorableInputDrawingLoader.readStorableDrawing(new StorableInput(bytes));
            Drawing drawing2 = DrawingFileHelper.loadDrawing(file
                                   .getAbsoluteFile(), displayer);
            if (drawing2 == null) {
                logger.warn("Something went wrong. Given Drawing could not be loaded: "
                            + drawing.getName());
                return false;
            }
            drawing.setName(drawing2.getName() + "[HEAD]");
            PNGDiffCommand diffCommand = new PNGDiffCommand();
            diffCommand.doDiff(displayer, drawing, drawing2, false);

            return true;
        } catch (IOException exception) {
            errorException(exception, "performing diff");
        } catch (SVNException exception) {
            errorException(exception, "performing diff");
        }

        return false;
    }

    public boolean log(File file) {
        return svnUtil.displayLog(file);
    }

    /**
     * Builds an SVN repository for a file.
     */
    private SVNRepository buildSVNRepository(File file) {
        try {
            SVNInfo info = wcClient.doInfo(file, SVNRevision.UNDEFINED);
            return new SVNRepository(info.getWorkingCopyRoot(), this);
        } catch (SVNException e) {
            return null;
        }
    }
}