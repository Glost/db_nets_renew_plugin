package de.renew.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import de.renew.call.StubCompiler;

import java.io.File;

import java.util.Enumeration;
import java.util.Vector;


/**
 * This Ant task facilitates the generation of Java source code
 * from Renew net stub definitions (<code>.stub</code> files).
 * It can operate on a file set and puts the generated source
 * into a similar tree under a configurable destination
 * directory.
 *
 * @author "Timo Carl" <6carl@informatik.uni-hamburg.de>
 **/
public class CreateStubsTask extends Task {
    private File destdir = null;
    private Vector<FileSet> filesets = new Vector<FileSet>();

    public void setDestdir(File dest) {
        this.destdir = dest;
    }

    public void addConfiguredFileset(FileSet fileset) {
        this.filesets.add(fileset);
    }

    public void execute() throws BuildException {
        StubCompiler compiler = new StubCompiler(destdir);

        Enumeration<FileSet> enumeration = filesets.elements();
        while (enumeration.hasMoreElements()) {
            FileSet fs = enumeration.nextElement();
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            File dir = ds.getBasedir();
            String[] stubs = ds.getIncludedFiles();
            for (int i = 0; i < stubs.length; i++) {
                File stub = new File(dir + File.separator + stubs[i]);
                try {
                    File target = compiler.compileStub(stub);
                    log("stub " + target + " generated.");
                } catch (Exception e) {
                    throw new BuildException("Stub file '" + stub
                                             + "' could not be generated:"
                                             + e.getMessage(), e);
                }
            }
        }
    }
}