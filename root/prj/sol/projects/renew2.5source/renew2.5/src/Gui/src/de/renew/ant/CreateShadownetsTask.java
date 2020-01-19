package de.renew.ant;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.GlobPatternMapper;
import org.apache.tools.ant.util.MergingMapper;
import org.apache.tools.ant.util.SourceFileScanner;

import CH.ifa.draw.standard.FigureException;

import de.renew.gui.CPNDrawing;
import de.renew.gui.FigureExceptionFactory;
import de.renew.gui.ShadowTranslator;

import de.renew.shadow.DefaultShadowNetLoader;
import de.renew.shadow.ShadowCompilerFactory;
import de.renew.shadow.ShadowNetLoader;
import de.renew.shadow.SyntaxException;

import de.renew.util.ClassSource;
import de.renew.util.StringUtil;

import java.io.File;

import java.lang.reflect.Constructor;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;


/**
 * This Ant task can transform Renew net drawings (<code>.rnw</code>) into
 * shadow net system files (<code>.sns</code>) suitable for Renew's net
 * loading mechanism. It can operate on a file set and puts each translated file
 * into the directory containing its source drawing.
 *
 * @author Timo Carl
 *
 * @author Michael Duvigneau
 *
 * @see ShadowTranslator
 */
public class CreateShadownetsTask extends Task {
    private String compilerName = null;
    private boolean timed = false;
    private File destdir = null;
    private File destfile = null;
    private Vector<FileSet> filesets = new Vector<FileSet>();
    private boolean compile = false;
    private boolean oneSystem = false;
    private Path classpath = null;
    private Path netpath = null;

    /**
     * Configures the root directory of the directory tree where all
     * <code>.sns</code> files are put into. This option is relevant only if
     * the <code>oneSystem</code> option is disabled.
     * <p>
     * <strong>This option is currently not implemented!</strong>
     * </p>
     *
     * @param dest
     *            a directory location
     */
    public void setDestdir(File dest) {
        this.destdir = dest;
    }

    /**
     * Configures the formalism of the generated <code>.sns</code> files. This
     * setting is optional unless you want to use the syntax check feature (<code>compile</code>
     * option).
     *
     * @param compilerName
     *            the class name of a compiler factory (e.g.
     *            <code>de.renew.formalism.java.JavaNetCompiler</code>).
     */
    public void setCompilerName(String compilerName) {
        this.compilerName = compilerName;
    }

    public void setTimed(boolean timed) {
        this.timed = timed;
    }

    /**
     * Enables syntax checking along with the format conversion. This option is
     * disabled by default.
     * <p>
     * If this option is enabled, the options <code>compilerName</code> and
     * <code>classpath</code> become mandatory.
     * </p>
     *
     * @param compile
     *            a <code>boolean</code> value; <code>true</code> enables
     *            syntax checking.
     */
    public void setCompile(boolean compile) {
        this.compile = compile;
    }

    /**
     * Configures whether one shadow net system (<code>.sns</code> file)
     * should be generated that comprises all nets. The resulting net system is
     * written to <code>destfile</code> (that option then becomes mandatory).
     * Such a net system is suitable for simulation setup.
     * <p>
     * If this option is disabled, each net drawing is transformed into a
     * separate <code>.sns</code> file in the <code>destdir</code> directory
     * tree. These files are suitable for the net loading mechanism (this is the
     * default behaviour of this task).
     * </p>
     *
     * @param oneSystem
     *            a <code>boolean</code> value; <code>true</code> merges all
     *            nets into one shadow net system.
     */
    public void setOneSystem(boolean oneSystem) {
        this.oneSystem = oneSystem;
    }

    /**
     * Configures the file where the generated shadow net system should be
     * written to. This setting is relevant and mandatory if and only if the
     * <code>oneSystem</code> option is enabled.
     *
     * @param destfile
     *            a <code>File</code>
     */
    public void setDestfile(File destfile) {
        this.destfile = destfile;
    }

    /**
     * Configures the set of drawings (<code>.rnw</code> files) to transform.
     *
     * @param fileset
     *            a <code>FileSet</code>
     */
    public void addConfiguredFileset(FileSet fileset) {
        this.filesets.add(fileset);
    }

    /**
     * Configures the Java classpath to use when compiling inscriptions of the
     * Java net formalism.
     *
     * @param classpath
     *            a <code>Path</code>
     */
    public void setClasspath(Path classpath) {
        if (this.classpath != null) {
            throw new BuildException("Duplicate classpath definition.");
        }
        this.classpath = classpath;
    }

    /**
     * Configures the Java classpath to use when compiling inscriptions of the
     * Java net formalism.
     *
     * @param classpathref
     *            a path <code>Reference</code>
     */
    public void setClasspathref(Reference classpathref) {
        if (this.classpath != null) {
            throw new BuildException("Duplicate classpath definition.");
        }
        this.classpath = (Path) classpathref.getReferencedObject(getProject());
    }

    public void setNetpathref(Reference netpath) {
        this.netpath = (Path) netpath.getReferencedObject(getProject());
    }

    /**
     * {@inheritDoc}
     */
    public void execute() throws BuildException {
        // basic plausibility checks
        if ((destfile != null) && (destdir != null)) {
            throw new BuildException("Cannot specify both the destfile "
                                     + "and destdir attributes.");
        }
        if ((destfile == null) && (destdir == null)) {
            throw new BuildException("Must specify one of destfile "
                                     + "or destdir attributes.");
        }

        // disable gui mode of renew
        String graphicStatus = System.setProperty("de.renew.noGraphics", "true");

        try {
            // minimal Renew logging configuration that forwards messages
            // to the Ant logging system
            Logger logger = Logger.getLogger("CH.ifa.draw");
            AntTaskLogAppender appender = AntTaskLogAppender.getInstance(this);
            logger.addAppender(appender);
            logger.setLevel(Level.INFO);
            logger = Logger.getLogger("de.renew");
            logger.addAppender(appender);
            logger.setLevel(Level.INFO);

            // Prepare the mapping from srcfiles to destfiles
            SourceFileScanner sfscanner = new SourceFileScanner(this);
            FileNameMapper mapper = null;
            boolean needAllNets = true;
            if (destfile != null) {
                MergingMapper mmapper = new MergingMapper();
                mmapper.setTo(destfile.getPath());
                mapper = mmapper;
                needAllNets = true;
            } else if (destdir != null) {
                GlobPatternMapper gpmapper = new GlobPatternMapper();
                gpmapper.setFrom("*.rnw");
                gpmapper.setTo(destdir + File.separator + "*.sns");
                mapper = gpmapper;

                needAllNets = false;
            }
            assert mapper != null : "No mapper configured.";

            // Collect drawing files
            CPNDrawing[] drawings = null;
            File[] destsnsfiles = null;
            Set<String> uptodateNetLocations = new HashSet<String>();
            Enumeration<FileSet> enumeration = filesets.elements();
            while (enumeration.hasMoreElements()) {
                FileSet fs = enumeration.nextElement();
                DirectoryScanner ds = fs.getDirectoryScanner(getProject());
                File dir = ds.getBasedir();
                String[] nets = ds.getIncludedFiles();
                if (!needAllNets) {
                    // Get the list of files that are newer than their
                    // destination.
                    String[] newnets = sfscanner.restrict(nets, dir, destdir,
                                                          mapper);

                    int j = 0;
                    for (int i = 0; i < nets.length; i++) {
                        if ((j < newnets.length) && nets[i].equals(newnets[j])) {
                            j++;
                        } else {
                            String[] snsfiles = mapper.mapFileName(nets[i]);
                            for (int k = 0; k < snsfiles.length; k++) {
                                String loc = StringUtil.convertToSystem(StringUtil
                                                                        .getPath(StringUtil
                                                                                 .convertToSlashes(snsfiles[k])));
                                if (uptodateNetLocations.add(loc)) {
                                    log("Added " + loc
                                        + " to net loader (from " + snsfiles[k]
                                        + ").", Project.MSG_VERBOSE);
                                }
                            }
                        }
                    }
                    assert (j == newnets.length) : "Loop has holes!? (" + j
                    + "!=" + newnets.length + ")";
                    nets = newnets;
                }

                // Transform the strings to files
                File[] netfiles = new File[nets.length];
                for (int i = 0, n = nets.length; i < n; i++) {
                    netfiles[i] = new File(dir, nets[i]);
                }
                CPNDrawing[] newDrawings = ShadowTranslator.readDrawings(netfiles);
                if (newDrawings == null) {
                    // nothing to do
                } else if (drawings == null) {
                    drawings = newDrawings;
                } else {
                    CPNDrawing[] mergedDrawings = new CPNDrawing[drawings.length
                                                  + newDrawings.length];
                    System.arraycopy(drawings, 0, mergedDrawings, 0,
                                     drawings.length);
                    System.arraycopy(newDrawings, 0, mergedDrawings,
                                     drawings.length, newDrawings.length);
                    drawings = mergedDrawings;
                }

                // Compute the destination file names, if needed
                if (destdir != null) {
                    int offset;
                    if (destsnsfiles == null) {
                        destsnsfiles = new File[nets.length];
                        offset = 0;
                    } else {
                        File[] enlargedArray = new File[destsnsfiles.length
                                               + nets.length];
                        System.arraycopy(destsnsfiles, 0, enlargedArray, 0,
                                         destsnsfiles.length);
                        offset = destsnsfiles.length;
                        destsnsfiles = enlargedArray;
                    }

                    for (int i = 0; i < nets.length; i++) {
                        String[] destination = mapper.mapFileName(nets[i]);
                        destsnsfiles[offset + i] = new File(destination[0]);
                        // Create parent directory.
                        log("Create parent directory.", Project.MSG_DEBUG);
                        File parentDir = destsnsfiles[offset + i].getParentFile();
                        log("parentDir = " + parentDir, Project.MSG_DEBUG);
                        if (parentDir != null) {
                            Mkdir mkdir = new Mkdir();
                            mkdir.setDir(parentDir);
                            try {
                                mkdir.execute();
                            } catch (NullPointerException e) {
                                log("Mkdir throws exception, was probably successful anyway: ",
                                    Project.MSG_DEBUG);
                                e.printStackTrace();
                            }
                        } else {
                            log("No parent directory for destination file "
                                + destsnsfiles[offset + i], Project.MSG_WARN);
                        }
                    }
                }
            }

            if (drawings == null) {
                throw new BuildException("No drawings specified.");
            }

            if (classpath != null) {
                ClassSource.setClassLoader(new AntClassLoader(CreateShadownetsTask.class
                                                              .getClassLoader(),
                                                              getProject(),
                                                              classpath, true));

            }
            ShadowCompilerFactory factory = getShadowCompiler();
            if (compile) {
                if (factory == null) {
                    throw new BuildException("Cannot compile - no valid compiler name configured: "
                                             + compilerName);
                } else {
                    log("Using compiler " + factory, Project.MSG_VERBOSE);
                }
                if (classpath == null) {
                    throw new BuildException("Cannot compile - no class path configured.");
                } else {
                    log("Using classpath " + classpath, Project.MSG_DEBUG);
                }
            }
            if (oneSystem) {
                if (destfile == null) {
                    throw new BuildException("Cannot create shadow net system - no destfile configured.");
                }
            }

            // Configure a net loader for nets that are up-to-date
            ShadowNetLoader netloader = null;
            Properties props = new Properties();
            boolean first = true;
            if (!needAllNets) {
                StringBuffer netpath = new StringBuffer();
                for (Iterator<String> locations = uptodateNetLocations.iterator();
                             locations.hasNext();) {
                    if (first) {
                        first = false;
                    } else {
                        netpath.append(File.pathSeparator);
                    }
                    netpath.append(locations.next());
                }

                props.setProperty("de.renew.netPath", netpath.toString());

                netloader = new DefaultShadowNetLoader(props);
            }
            if (netpath != null) {
                StringBuffer netPath = new StringBuffer(props.getProperty("de.renew.netPath",
                                                                          "")
                                                             .toString());

                String[] liste = netpath.list();
                for (int i = 0; i < liste.length; i++) {
                    if (first) {
                        first = false;
                    } else {
                        netPath.append(File.pathSeparator);
                    }
                    netPath.append(liste[i]);
                }
                props.setProperty("de.renew.netPath", netPath.toString());
            }
            netloader = new DefaultShadowNetLoader(props);
            // Let's finally do the work
            try {
                if (oneSystem) {
                    ShadowTranslator.writeSingleShadow(factory, compile,
                                                       drawings, destfile);
                } else {
                    ShadowTranslator.writeShadows(factory, compile, drawings,
                                                  destsnsfiles, netloader);
                }
            } catch (SyntaxException e) {
                BuildException toThrow = null;
                try {
                    FigureException figEx = FigureExceptionFactory
                                                .createFigureException(e);
                    System.out.println("CreateShadownetsTask.execute : "
                                       + figEx.errorDrawing.getFilename()); //DEBUG
                    Location location = new Location(figEx.errorDrawing.getFilename()
                                                                       .toString(),
                                                     figEx.line, figEx.column);
                    toThrow = new BuildException("Net Compilation "
                                                 + figEx.getMessage(), e,
                                                 location);
                } catch (Exception ignoredEx) {
                    System.out.println("CreateShadownetsTask.execute : "); //DEBUG
                    toThrow = new BuildException(e);
                }
                throw toThrow;
            }
        } finally {
            // set property back to the value it had before this task
            if (graphicStatus != null) {
                System.setProperty("de.renew.noGraphics", graphicStatus);
            } else {
                System.clearProperty("de.renew.noGraphics");
            }
        }
    }

    /**
     * Creates a shadow compiler based on the <code>compilerName</code>
     * setting.
     *
     * @return the <code>ShadowCompilerFactory</code> derived from the
     *         <code>compilerName</code> setting.
     */
    private ShadowCompilerFactory getShadowCompiler() {
        if (compilerName == null) {
            return null;
        }
        try {
            // We use the standard class loader because the plugin system
            // is not necessarily running.
            if (timed) {
                Class<?> c = ClassSource.classForName(compilerName);
                Constructor<?> constructor = c.getConstructor(boolean.class,
                                                              boolean.class,
                                                              boolean.class);
                return (ShadowCompilerFactory) constructor.newInstance(true,
                                                                       true,
                                                                       true);
            } else {
                Class<?> c = ClassSource.classForName(compilerName);
                return (ShadowCompilerFactory) c.newInstance();
            }
        } catch (Exception e) {
            this.log("Could not create ShadowNetCompiler. Reason:\n" + e);
            return null;
        }
    }
}