package de.renew.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.taskdefs.Replace;
import org.apache.tools.ant.taskdefs.Replace.NestedString;
import org.apache.tools.ant.taskdefs.optional.javacc.JavaCC;
import org.apache.tools.ant.types.FileSet;

import java.io.File;


/**
 * Enhances the <code>JavaCC</code> task delivered with Ant to
 * work on a file set. The generated Java source of all processed
 * grammars can be put in a destination directory tree similar to
 * the one where the grammar files are kept.
 *
 * @author "Timo Carl" <6carl@informatik.uni-hamburg.de>
 *
 * @see org.apache.tools.ant.taskdefs.optional.javacc.JavaCC
 **/
public class EnhancedJavaCCTask extends Task {
    private File javacchome;
    private File destdir;
    private FileSet fileset;
    private File srcbase;
    private String jdkversion;

    public void init() throws BuildException {
        super.init();
    }

    public void setDestdir(File destdir) {
        this.destdir = destdir;
    }

    public void setJavacchome(File javacc) {
        this.javacchome = javacc;
    }

    public void addConfiguredFileset(FileSet fileset) {
        this.fileset = fileset;
        this.srcbase = fileset.getDir(getProject());
    }

    public void setSrcbase(File srcbase) {
        this.srcbase = srcbase;
    }

    public void setJDKversion(String jdkversion) {
        this.jdkversion = jdkversion;
    }

    public void execute() throws BuildException {
        DirectoryScanner ds = fileset.getDirectoryScanner(getProject());
        String[] javaccFiles = ds.getIncludedFiles();
        File basedir = ds.getBasedir();
        for (int i = 0; i < javaccFiles.length; i++) {
            File f = new File(basedir.getAbsolutePath() + File.separator
                              + javaccFiles[i]);
            if (!f.isFile()) {
                throw new BuildException("javacc-File " + f
                                         + " does not exist!");
            }
            executeJavaCC(f);
        }
    }

    private void executeJavaCC(File javaccFile) throws BuildException {
        File target = javaccFile;
        String targetDir = javaccFile.getParent();
        String packageDir = targetDir.substring(srcbase.getAbsolutePath()
                                                       .length());
        File directory = new File(destdir.getAbsolutePath() + File.separator
                                  + packageDir);

        // Create dir if not exist
        if (!directory.isDirectory()) {
            Mkdir mkdir = (Mkdir) getProject().createTask("mkdir");
            mkdir.setDir(directory);
            mkdir.init();
            mkdir.execute();
        }
        JavaCC task = (JavaCC) getProject().createTask("javacc");
        task.setJavacchome(javacchome);
        task.setTarget(target);
        task.setOutputdirectory(directory);
        if (jdkversion != null) {
            task.setJDKversion(jdkversion);
        }
        task.init();
        task.execute();

        String javaccfilename = javaccFile.getName();
        if (javaccfilename == null || !javaccfilename.endsWith(".jj")) {
            log("Cannot determine filename of token manager to add @SuppressWarnings. "
                + "Skipping this step.", Project.MSG_WARN);
        } else {
            String tokenmanagerfilename = javaccfilename.substring(0,
                                                                   javaccfilename
                                                                   .length()
                                                                   - 3)
                                          + "TokenManager.java";
            File tokenmanagerfile = new File(directory, tokenmanagerfilename);
            log("Trying to insert @SuppressWarnings(\"unused\") in "
                + tokenmanagerfile, Project.MSG_VERBOSE);
            Replace rtask = (Replace) getProject().createTask("replace");
            rtask.setFile(tokenmanagerfile);
            NestedString search = rtask.createReplaceToken();
            search.addText("/** Token Manager. */\npublic class");
            NestedString replace = rtask.createReplaceValue();
            replace.addText("/** Token Manager. */\n@SuppressWarnings(\"unused\")\npublic class");
            rtask.execute();
        }
    }
}