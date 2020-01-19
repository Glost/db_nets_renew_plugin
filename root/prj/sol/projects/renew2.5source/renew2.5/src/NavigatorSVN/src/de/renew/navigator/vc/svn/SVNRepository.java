package de.renew.navigator.vc.svn;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;

import de.renew.navigator.vc.AbstractRepository;
import de.renew.navigator.vc.Commit;

import java.io.File;


/**
 * @author Konstantin Simon Maria Möllers
 * @version 0.1
 */
public class SVNRepository extends AbstractRepository {
    protected final SVNVersionControl svn;
    protected final SVNInfo svnInfo;

    public SVNRepository(File rootDirectory, SVNVersionControl versionControl) {
        super(rootDirectory, versionControl);
        this.svn = versionControl;
        this.svnInfo = retrieveSVNInfo();
        lastCommit = retrieveLastCommit();
        update();
    }

    @Override
    public String getBranch() {
        final String branchRelative = svnInfo.getURL().toString();
        final String remoteURL = getRemoteURL();
        if (branchRelative.indexOf(remoteURL) == 0) {
            return branchRelative.substring(remoteURL.length());
        }

        return branchRelative;
    }

    @Override
    public String getRemoteURL() {
        return svnInfo.getRepositoryRootURL().toString();
    }

    @Override
    protected Commit retrieveLastCommit() {
        try {
            // Retrieve generic commit from SVN log.
            final Commit commit = new Commit();
            svn.getLogClient().doLog(new File[] { rootDirectory },
                                     SVNRevision.COMMITTED, null, null, false,
                                     false, 1,
                                     new ISVNLogEntryHandler() {
                    @Override
                    public void handleLogEntry(SVNLogEntry svnLogEntry)
                            throws SVNException {
                        commit.setAuthor(svnLogEntry.getAuthor());
                        commit.setDate(svnLogEntry.getDate());
                        commit.setMessage(svnLogEntry.getMessage());
                        commit.setRevision(String.valueOf(svnLogEntry
                            .getRevision()));
                    }
                });

            return commit;
        } catch (SVNException e) {
            return null;
        }
    }

    @Override
    public void update() {
        final ISVNStatusHandler handler = new ISVNStatusHandler() {
            @Override
            public void handleStatus(SVNStatus status)
                    throws SVNException {
                SVNStatusType statusType = status
                                              .getContentsStatus();

                File newFile = status.getFile();
                if (statusType == SVNStatusType.STATUS_ADDED) {
                    added.add(newFile);
                    return;
                }

                if (statusType == SVNStatusType.STATUS_MODIFIED) {
                    modified.add(newFile);
                    while (!newFile.getParentFile().equals(rootDirectory)) {
                        newFile = newFile.getParentFile();
                        modified.add(newFile);
                    }
                    return;
                }

                if (statusType == SVNStatusType.STATUS_IGNORED) {
                    ignored.add(newFile);
                }
            }
        };

        // Try to retrieve files from status.
        try {
            svn.getStatusClient()
               .doStatus(rootDirectory, SVNRevision.HEAD, SVNDepth.INFINITY,
                         false, false, true, false, handler, null);
        } catch (SVNException ignored) {
        }
    }

    /**
     * Retrieves the SVN info of this repository.
     */
    private SVNInfo retrieveSVNInfo() {
        SVNInfo svnInfo;
        try {
            svnInfo = svn.getWcClient().doInfo(this.rootDirectory, null);
        } catch (SVNException e) {
            svnInfo = null;
        }
        return svnInfo;
    }
}