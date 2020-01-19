package de.renew.navigator.vc.git;

import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import org.gitective.core.BlobUtils;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.DrawingFileHelper;
import CH.ifa.draw.io.StatusDisplayer;
import CH.ifa.draw.io.StorableInputDrawingLoader;

import CH.ifa.draw.util.StorableInput;

import de.renew.imagenetdiff.PNGDiffCommand;

import de.renew.logging.CliColor;

import de.renew.navigator.vc.AbstractVersionControl;
import de.renew.navigator.vc.Repository;
import de.renew.navigator.vc.StdoutStatusDisplayer;

import java.io.File;
import java.io.IOException;

import java.net.URI;


/**
 * @author Konstantin Simon Maria Moellers
 * @version 2015-10-13
 */
public class GitVersionControl extends AbstractVersionControl {

    @Override
    public boolean diff(File file) {
        logger.debug("diff with head: " + file.getAbsolutePath());
        try {
            final GitRepository repository = buildGitRepository(file);
            URI relative = repository.makeRelativeURI(file);
            if (logger.isDebugEnabled()) {
                logger.debug("Git Repository Branch:  "
                             + repository.getBranch());
                logger.debug("Git file path:          " + file.getPath());
                logger.debug("Git relative file path: " + relative.getPath());
            }
            String bytes = BlobUtils.getContent(repository.fileRepository,
                                                "HEAD", relative.getPath());

            // No content found?
            if (bytes == null) {
                if (logger.isInfoEnabled()) {
                    logger.info("Git: There is no HEAD here. File possibly not in repository. Received null.");
                }
                return false;
            }

            Drawing drawing = StorableInputDrawingLoader.readStorableDrawing(new StorableInput(bytes));
            final StatusDisplayer displayer = new StdoutStatusDisplayer();

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
        } catch (AmbiguousObjectException exception) {
            errorException(exception, "performing diff with head");
        } catch (MissingObjectException exception) {
            errorException(exception, "performing diff with head");
        } catch (IncorrectObjectTypeException exception) {
            errorException(exception, "performing diff with head");
        } catch (IOException exception) {
            errorException(exception, "performing diff with head");
        }

        return false;
    }

    /**
     * Prints the log for a file out to Stdout.
     */
    public boolean log(File file) {
        try {
            boolean result = false;
            GitRepository repository = buildGitRepository(file);
            LogCommand log2 = repository.git.log();

            URI relative = repository.makeRelativeURI(file);
            logger.info("Branch: " + repository.getBranch() + " File: "
                        + relative + "\n Revisions: ");

            Iterable<RevCommit> call = log2.addPath(relative.getPath()).call();
            for (RevCommit revCommit : call) {
                System.out.println("\n\n"
                                   + CliColor.color(revCommit.toString(),
                                                    CliColor.BLUE) + "\n"
                                   + CliColor.color("Author: "
                                                    + revCommit.getAuthorIdent()
                                                               .getEmailAddress()
                                                    + "\n" + "Date:   "
                                                    + revCommit.getAuthorIdent()
                                                               .getWhen(),
                                                    CliColor.WHITE) + "\n\n"
                                   + revCommit.getFullMessage()
                                              .replaceAll("(?m)^", "\t"));
                result = true;
            }

            return result;
        } catch (NoHeadException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    protected Repository buildRepository(File fileInRepository) {
        return buildGitRepository(fileInRepository);
    }

    /**
     * Builds a Git repository for a given file.
     *
     * @param fileInRepository the file to build the repository to.
     * @return a Git repository or <code>null</code>.
     */
    protected GitRepository buildGitRepository(File fileInRepository) {
        logger.debug("Locating Git repository for [" + fileInRepository + "]");
        final FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        repositoryBuilder.findGitDir(fileInRepository)
                         .setMustExist(true)
                         .readEnvironment();

        // Check if a Git directory has been found.
        if (null == repositoryBuilder.getGitDir()) {
            logger.debug("This is not in a Git repo   [" + fileInRepository + "]");
            return null;
        }

        try {
            FileRepository repository = repositoryBuilder.build();

            System.out.println(repository);
            return new GitRepository(repository, this);
        } catch (IOException e) {
            logger.error("Unable to locate Git repo", e);
            return null;
        } catch (IllegalArgumentException e) {
            logger.error("Unable to locate Git repo", e);
            return null;
        }
    }

    /**
     * Prints out a list of revisions for a file.
     */
    public boolean displayListForFile(File file) {
        try {
            boolean result = false;
            GitRepository repository = buildGitRepository(file);
            LogCommand log2 = repository.git.log();
            URI relative = repository.makeRelativeURI(file);
            logger.info("Branch: " + repository.getBranch());
            logger.info("File:   " + relative);

            Iterable<RevCommit> call = log2.addPath(relative.getPath()).call();
            for (RevCommit revCommit : call) {
                logger.info("Revisions: " + revCommit.toString());
                result = true;
            }

            return result;
        } catch (NoHeadException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return false;
    }
}
