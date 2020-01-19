package de.renew.navigator.vc.git;

import org.apache.log4j.Logger;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.storage.file.FileRepository;

import de.renew.navigator.vc.AbstractRepository;
import de.renew.navigator.vc.Commit;

import de.renew.util.StringUtil;

import java.io.File;
import java.io.IOException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * @author Konstantin Simon Maria Möllers
 * @version 0.1
 */
public class GitRepository extends AbstractRepository {

    /**
     * Log4j logger instance.
     */
    public static final Logger logger = Logger.getLogger(GitRepository.class);
    protected final Git git;
    protected final FileRepository fileRepository;

    public GitRepository(FileRepository fileRepository,
                         GitVersionControl versionControl) {
        super(fileRepository.getWorkTree(), versionControl);
        this.fileRepository = fileRepository;
        git = new Git(fileRepository);
        lastCommit = retrieveLastCommit();
        update();
    }

    @Override
    public String getBranch() {
        try {
            return fileRepository.getBranch();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String getRemoteURL() {
        final FileBasedConfig config = fileRepository.getConfig();
        final Set<String> remotes = config.getSubsections("remote");
        final Set<String> svnRemotes = config.getSubsections("svn-remote");

        // Create a new Set of URLs based on Remotes / SVN Remotes.
        final HashSet<String> urls = new HashSet<String>(remotes.size()
                                                         + svnRemotes.size());
        for (String remote : remotes) {
            urls.add(config.getString("remote", remote, "url"));
        }
        for (String svnRemote : svnRemotes) {
            urls.add(config.getString("svn-remote", svnRemote, "url"));
        }

        return StringUtil.join(urls, ",");
    }

    @Override
    public void update() {
        modified.clear();
        added.clear();
        ignored.clear();

        try {
            final Status status = git.status().call();
            modified.addAll(makeFileSet(status.getModified(), true));
            added.addAll(makeFileSet(status.getAdded(), false));
            ignored.addAll(makeFileSet(status.getUntracked(), false));
        } catch (GitAPIException ignored) {
        }
    }

    /**
     * Tries to retrieve the last commit on this repository.
     */
    protected Commit retrieveLastCommit() {
        try {
            final Iterator<RevCommit> iterator = git.log().call().iterator();
            if (iterator.hasNext()) {
                RevCommit revCommit = iterator.next();

                // Create common commit from Git commit.
                final Commit commit = new Commit();
                commit.setAuthor(revCommit.getAuthorIdent().getName());
                commit.setDate(revCommit.getCommitTime());
                commit.setMessage(revCommit.getFullMessage());
                commit.setRevision(revCommit.getName());

                return commit;
            }
        } catch (GitAPIException e) {
            logger.error("Could not get last commit", e);
        }

        return null;
    }

    /**
     * Converts a relative string file set to a true set of files.
     */
    private Set<File> makeFileSet(Set<String> gitFileSet, boolean recursive) {
        HashSet<File> target = new HashSet<File>();
        String workTree = getRootDirectory().getAbsolutePath();
        for (String file : gitFileSet) {
            final String filename = workTree + File.separatorChar + file;
            File newFile = new File(filename);
            if (newFile.exists()) {
                target.add(newFile);
                while (recursive
                               && !newFile.getParentFile().equals(rootDirectory)) {
                    newFile = newFile.getParentFile();
                    target.add(newFile);
                }
            }
        }

        return target;
    }
}