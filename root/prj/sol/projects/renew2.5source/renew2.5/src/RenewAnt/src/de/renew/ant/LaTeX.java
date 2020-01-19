package de.renew.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.IOException;


/**
   A class that runs LaTeX and BIBTeX.<p>

   <b>Implementation plan:</b><br>
   <ol>

     <li> Execution of LaTeX and assorted programs by instantiating of
     <code>Execute</code>. An example could be <code>Javac</code>.

     <li> Deletion of intermediate files such as aux, toc, etc, via
     pattern sets, this is shown in <code>MatchingTask</code>. This
     easily allows addition of other pattern in the ant-file depending
     on the document.

     <li> Parsing the output of LaTeX should give hints, whether we
     need an additional run. Use a counter to abort from endless loops
     which may result from using the package <code>varioref</code>.

     <li> Test via junit at least the construction of the list of
     deletable files. Perhaps go further and test the command line or
     even the results of latex commands.

   </ol>
   @author Klaus Alfert <alfert@shire.prima.de>
*/
public class LaTeX extends Task {
    // Attributes
    private String latexfile = null;
    private String bibtexfile = null;
    private File latexdir = null;
    private boolean bibtex = false;
    private boolean pdftex = false;
    private boolean psgztex = false;
    private boolean clean = false;
    private Delete delete = null;
    private final String[] deletePatterns = { "*.aux", "*.log", "*.toc", "*.lof", "*.lot", "*.bbl", "*.blg", "*.out" };

    /** Constructor. Simply calls <code>super()</code>. */
    public LaTeX() {
        super();
    }

    /** Initialiazes some default-values which need a running environment. */
    public void init() {
        this.latexdir = getProject().getBaseDir();
    }

    /** The LaTeX main file*/
    public void setLatexfile(String filename) {
        latexfile = filename;
        bibtexfile = latexfile.substring(0, latexfile.lastIndexOf(".tex"));
    }

    /** The directory in which the main-file resides. */
    public void setDirectory(File dir) {
        latexdir = dir;
    }

    /** Set the BIBTeX flag */
    public void setBibtex(boolean bibtex) {
        this.bibtex = bibtex;
    }

    /** Set the PDFLaTeX flag */
    public void setPdftex(boolean pdftex) {
        this.pdftex = pdftex;
    }

    /** Set the psgztex flag */
    public void setPsgztex(boolean psgztex) {
        this.psgztex = psgztex;
    }

    /** Set the clean flag */
    public void setClean(boolean clean) {
        this.clean = clean;
    }

    public void execute() throws BuildException {
        this.log("Running LaTeX now!", Project.MSG_VERBOSE);
        this.dump();
        log("Calling latex on " + latexfile);
        this.runLaTeX();
        if (bibtex) {
            log("bibtex...");
            this.runBIBTeX();
        }
        log("latex");
        this.runLaTeX();

        log("latex");
        this.runLaTeX();

        if (psgztex) {
            log("Creating ps from dvi file...");
            this.runDvips();
            if (pdftex) {
                log("Creating pdf from ps file");
                this.runPs2pdf();
            }
            log("Running gzip on ps file");
            this.runGzip();
        }
        if (delete == null) {
            delete = this.createDefaultDelete();
        }
        this.log("run Delete", Project.MSG_VERBOSE);
        delete.execute();
        log("Done.");
    }

    /** Creates the nested delete element */
    public Object createDelete() {
        log("Delete is created");
        delete = (Delete) getProject().createTask("delete");
        return delete;
    }

    /** Creates the default delete element */
    private Delete createDefaultDelete() {
        Delete delete = null;
        delete = (Delete) getProject().createTask("delete");
        FileSet fs = new FileSet();
        fs.setDir(this.latexdir);
        for (int i = 0; i < this.deletePatterns.length; i++) {
            fs.createInclude().setName(deletePatterns[i]);
        }
        delete.addFileset(fs);
        delete.setVerbose(true);
        return delete;
    }

    /** Constructs and runs LaTeX-command. */
    private final int runLaTeX() throws BuildException {
        String latex = "latex";
        String[] command = { latex, "\\nonstopmode\\input{" + latexfile + "}" };
        return this.runCommand(command, false);
    }

    /** Constructs and runs BibTeX-command. */
    private final int runBIBTeX() throws BuildException {
        String[] command = { "bibtex", bibtexfile };
        return this.runCommand(command, false);
    }

    /** Constructs and runs dvips-command. */
    private final int runDvips() throws BuildException {
        String[] command = { "dvips", "-D600", "-o", bibtexfile + ".ps", bibtexfile };
        return this.runCommand(command, true);
    }

    private final int runGzip() throws BuildException {
        String[] command = { "gzip", "-f", bibtexfile + ".ps" };
        return this.runCommand(command, false);
    }

    private final int runPs2pdf() throws BuildException {
        String[] command = { "ps2pdf", bibtexfile + ".ps" };
        return this.runCommand(command, false);
    }

    /** Runs the command.
        The output of the command is logged at the verbose level.
        @param command the command.
        @param wordy   whether the command tends to scroll on the
                       standard error channel.
     */
    private final int runCommand(String[] command, boolean wordy)
            throws BuildException {
        int exitCode = 0;
        StringBuffer com = new StringBuffer();
        for (int i = 0; i < command.length; i++) {
            if (i > 0) {
                com.append(' ');
            }
            com.append(command[i]);
        }
        log("running command: " + com.toString(), Project.MSG_VERBOSE);
        try {
            Execute exec = null;
            if (wordy) {
                exec = new Execute(new LogStreamHandler(this,
                                                        Project.MSG_DEBUG,
                                                        Project.MSG_DEBUG), null);
            } else {
                exec = new Execute(new LogStreamHandler(this,
                                                        Project.MSG_DEBUG,
                                                        Project.MSG_WARN), null);
            }


            exec.setWorkingDirectory(latexdir);
            exec.setCommandline(command);


            // allow Ant to find the antRun script if necessary
            exec.setAntRun(getProject());
            exitCode = exec.execute();
        } catch (IOException exp) {
            throw new BuildException(exp);
        }
        log("command exitcode = " + exitCode, Project.MSG_VERBOSE);
        return exitCode;
    }

    /** Dumps the local attributes. */
    private void dump() {
        this.log("latexfile  = " + latexfile, Project.MSG_VERBOSE);
        this.log("bibtexfile = " + latexfile, Project.MSG_VERBOSE);
        this.log("latexdir   = " + latexdir, Project.MSG_VERBOSE);
        this.log("bibtex     = " + bibtex, Project.MSG_VERBOSE);
        this.log("bibtex     = " + bibtex, Project.MSG_VERBOSE);
        this.log("clean      = " + clean, Project.MSG_VERBOSE);
        this.log("delete     = " + delete, Project.MSG_VERBOSE);
    }
}
