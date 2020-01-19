/**
 *
 */
package de.renew.svn;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.File;


/** Utility for the usage of Renew Drawings with subversion.
 *
 * @author cabac
 *
 */
public class SVNUtil {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(SVNUtil.class);
    private static SVNClientManager svnManager;
    private static SVNLogClient logClient;
    private static SVNWCClient wcClient;
    static private SVNUtil instance;

    static public SVNUtil getInstance() {
        if (instance == null) {
            instance = new SVNUtil();
        }
        return instance;
    }

    /**
     *
     */
    private SVNUtil() {
        ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
        svnManager = SVNClientManager.newInstance(options);
        logClient = svnManager.getLogClient();
        wcClient = svnManager.getWCClient();
    }

    //FIXME: authentication
    //FIXME: display text
    public boolean displayLog(File file) {
        boolean result = false;
        ISVNLogEntryHandler logHandler = new ISVNLogEntryHandler() {
            @Override
            public void handleLogEntry(SVNLogEntry entry)
                    throws SVNException {
                System.out.println(entry.getAuthor());
                System.out.println(entry.getMessage());
            }
        };
        try {
            logClient.doLog(new File[] { file }, SVNRevision.HEAD,
                            SVNRevision.create(1000), SVNRevision.HEAD, false,
                            false, false, 20, new String[] {  }, logHandler);
        } catch (SVNException e) {
            logger.error(e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug(SVNUtil.class.getSimpleName() + ": " + e);
            }
        }
        return result;
    }

    public boolean displayInfo(File file) {
        boolean result = false;
        SVNInfo info = null;
        try {
            info = wcClient.doInfo(file, SVNRevision.BASE);
            result = true;
        } catch (SVNException e) {
            logger.error(e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug(SVNUtil.class.getSimpleName() + ": " + e);
            }
        }
        if (info != null) {
            System.out.println("SvnInfo for file: " + file.getName() + " "
                               + info.getRevision() + " " + info.getAuthor()
                               + " " + info.getCommittedDate());
        }
        return result;
    }
}