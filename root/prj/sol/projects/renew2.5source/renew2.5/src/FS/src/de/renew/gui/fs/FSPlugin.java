package de.renew.gui.fs;

import de.uni_hamburg.fs.FeatureStructure;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.DrawApplication;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import CH.ifa.draw.standard.CompositeFigure;
import CH.ifa.draw.standard.StandardDrawing;

import de.renew.formalism.FormalismPlugin;
import de.renew.formalism.efsnet.EFSNetCompiler;
import de.renew.formalism.fs.FSNetCompiler;
import de.renew.formalism.fs.FSNetWithoutConceptsCompiler;
import de.renew.formalism.fsnet.XFSNetCompiler;

import de.renew.gui.FigureCreator;
import de.renew.gui.FigureCreatorHolder;
import de.renew.gui.FormalismGuiPlugin;
import de.renew.gui.GuiPlugin;
import de.renew.gui.TextFigureCreator;

import de.renew.net.NetInstance;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginAdapter;
import de.renew.plugin.PluginException;
import de.renew.plugin.PluginManager;
import de.renew.plugin.PluginProperties;

import de.renew.remote.ObjectAccessor;
import de.renew.remote.ObjectAccessorImpl;

import de.renew.util.Value;

import java.net.URL;

import java.util.Enumeration;
import java.util.Iterator;


/**
 * The FS (Feature Structure) plug-in comprises some net formalisms and
 * provides UML-like figures for information-oriented modeling.
 *
 * <h3>Formalisms</h3>
 * The net formalisms supported by this plug-in are as follows
 * (for details, see <a href="#Diss">[1]</a>):
 * <ul>
 * <li>
 *   <b>Basic FS-nets (no compiler):</b>
 *   Uses feature structures to inscribe places, arcs and transitions.
 *   Place markings are multisets over feature structures. Bindings are
 *   determined through unification of all feature structures used in the
 *   locality of a transition.
 * </li>
 * <li>
 *   <b>Java-nets with FS tokens (Java+FS Net Compiler):</b>
 *   FS expressions are allowed in addition to Java expressions.
 *   There is no support for FS transition rules.
 * </li>
 * <li>
 *   <b>Elementary FS-nets (EFS Net Compiler):</b>
 *   One-safe FS-nets without sets of feature structures. The formalism
 *   operates in either of two modes, namly value or reference semantics.
 *   Both come with formally defined interleaving, step and process
 *   semantics.
 * </li>
 * <li>
 *   <b>Higher FS-nets (XFS Net Compiler):</b>
 *   FS-nets extended with reference semantics, net instances, synchronous
 *   channels and Java integration.
 * </li>
 * </ul>
 * <b>However, for the time being, the plug-in does not publicly announce
 * all these formalisms.</b> Some have not been adopted to the new architecture
 * of the Formalism plug-in in Renew 2.0.
 *
 * <h3>Editor extensions</h3>
 * <p>
 * The FS plug-in provides figures and a tool bar to draw feature
 * structures like UML 1 class diagrams (enhanced by graphical distinction
 * of disjunctive and conjunctive is-a-relations), in AVM-notation or as
 * feature node graphs.
 * </p>
 * <p>
 * Furthermore, the UML class diagram notation can be used to inspect the
 * current state of Java objects during a running simulation in any
 * formalism. This is known in the Renew User Guide
 * <a href="#RenewUG">[2]</a> as display of "expanded tokens".
 * </p>
 *
 * <h3>References</h3>
 * <ul>
 * <li><a name="Diss"></a>[1]
 *   Frank Wienberg:
 *   <i>Informations- und prozessorientierte Modellierung verteilter Systeme
 *   auf der Basis von Feature-Structure-Netzen.</i>
 *   Dissertation. Fachbereich Informatik, Universitaet Hamburg, 2001.
 * </li>
 * <li><a name="RenewUG"></a>[2]
 *   Olaf Kummer, Frank Wienberg, Michael Duvigneau:
 *   <i>Renew - User Guide.</i>
 *   Shipped with Renew.
 * </li>
 * </ul>
 *
 * @author Joern Schumacher
 * @author Michael Duvigneau
 */
public class FSPlugin extends PluginAdapter {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(FSPlugin.class);
    public static final String FSNET_COMPILER = "Java+FS Net Compiler (untested)";
    public static final String FSNET_WOC_COMPILER = "Java+FS Net Compiler - No Concepts (untested)";
    public static final String EFSNET_COMPILER = "EFS Net Compiler (untested)";
    public static final String XFSNET_COMPILER = "XFS Net Compiler";

    /**
     * The name of the property that initially configures the render mode
     * for FS figures.  The name is {@value}.
     * @see #getUmlRenderMode
     * @see #setUmlRenderMode
     **/
    public static final String UML_RENDER_MODE_PROP = "de.renew.gui.fs.umlMode";

    /**
     * The name of the property that enables experimental FSNet compilers.
     * The property is evaluated at initalisation time only.
     * The name is {@value}.
     **/
    public static final String FS_EXPERIMENTAL_COMPILERS_PROP = "de.renew.gui.fs.showExperimentalCompilers";

    /**
     * Remembers our figure creator for later cleanup.
     **/
    private FigureCreator fsFigureCreator = null;

    /**
     * Remembers our text figure creator for later cleanup.
     **/
    private TextFigureCreator fsTextFigureCreator = null;

    /**
     * Flag that determines whether FS figures are rendered like UML
     * classes and objects.
     * <p>
     * This field is semi-coupled with the plug-in property
     * {@link #UML_RENDER_MODE_PROP} (see comments of the three get/set
     * methods).
     * </p>
     * @see #readInitialRenderMode
     * @see #getUmlRenderMode
     * @see #setUmlRenderMode
     **/
    private boolean umlRenderMode;

    /**
     * Create an instance of the FS plugin.
     * @see PluginAdapter#PluginAdapter(URL)
     */
    public FSPlugin(URL location) throws PluginException {
        super(location);
        readInitialRenderMode();
    }

    /**
     * Create an instance of the FS plugin.
     * @see PluginAdapter#PluginAdapter(PluginProperties)
     */
    public FSPlugin(PluginProperties props) {
        super(props);
        readInitialRenderMode();
    }

    /**
     * Sets the initial value of the field {@link #umlRenderMode} in
     * accordance to the user-configured value of the plug-in property
     * {@link #UML_RENDER_MODE_PROP}.  This method has to be called exactly
     * once by each constructor.  It does not update any existing FS
     * figures because there should not exist any when the
     * <code>FSPlugin</code> is instantiated.
     **/
    private void readInitialRenderMode() {
        umlRenderMode = getProperties().getBoolProperty(UML_RENDER_MODE_PROP);
    }

    /**
     * Tells wether the UML render mode for FS figures is enabled.
     * <p>
     * The initial setting is determined from the plug-in property
     * {@link #UML_RENDER_MODE_PROP}.  However, this method does not
     * reflect later changes to the property, it returns the relevant
     * state stored within this object.
     * </p>
     * @return <code>true</code> if FS figures are rendered like objects in UML,
     *         <code>false</code> if not.
     * @see #setUmlRenderMode
     */
    public boolean getUmlRenderMode() {
        return this.umlRenderMode;
    }

    /**
     * Switches the UML render mode for FS figures on or off, respectively.
     * <p>
     * The initial setting is determined from the plug-in property
     * {@link #UML_RENDER_MODE_PROP}.  This method updates the property, but
     * property changes from any third party have no effect.
     * </p>
     * <p>
     * <b>Attention:</b>
     * This method has a huge side effect because it re-renders all FS
     * figures in all drawings of the editor, thus clearing the current
     * selection and the undo/redo history of the drawings.  The impact is
     * kept to a minimum, it touches only drawings with FS figures.
     * </p>
     * @param newUmlRenderMode  wether to turn the UML rendering on.
     * @see #setUmlRenderMode
     */
    public void setUmlRenderMode(final boolean newUmlRenderMode) {
        if (umlRenderMode != newUmlRenderMode) {
            umlRenderMode = newUmlRenderMode;
            getProperties()
                .setProperty(UML_RENDER_MODE_PROP,
                             Boolean.toString(umlRenderMode));

            // Now mark dirty all FSFigures in all drawings, if the editor is running:
            DrawApplication editor = DrawPlugin.getGui();
            if (editor != null) {
                Enumeration<Drawing> drawings = editor.drawings();
                while (drawings.hasMoreElements()) {
                    StandardDrawing drawing = (StandardDrawing) drawings
                                                  .nextElement();
                    boolean changed = changeFSFigures(drawing);
                    if (changed) {
                        DrawingView view = editor.getView(drawing);
                        if (view != null) {
                            view.clearSelection();
                            view.checkDamage();
                        }

                        // To avoid inconsistencies: 
                        editor.getUndoRedoManager().clearUndoHistory(drawing);
                    }
                }
            }
        }
    }

    /**
     * Marks all FS figures in the given figure as dirty, and recursively
     * traverses nested <code>CompositeFigure</code>s.
     * @param figure  the <code>CompositeFigure</code> containing all
     *                figures to process
     * @return  <code>true</code> if any figure has been marked dirty.
     **/
    private boolean changeFSFigures(CompositeFigure figure) {
        boolean changed = false;
        FigureEnumeration figs = figure.figures();
        while (figs.hasMoreElements()) {
            Figure fig = figs.nextFigure();
            if (fig instanceof FSFigure) {
                fig.willChange();
                ((FSFigure) fig).markDirty();
                fig.changed();
                changed = true;
            } else if (fig instanceof CompositeFigure) {
                changed = changed | changeFSFigures((CompositeFigure) fig);
            }
        }
        return changed;
    }

    /**
     * Initializes the FS plug-in and registers its GUI extensions.
     **/
    public void init() {
        FigureCreatorHolder fc = GuiPlugin.getCurrent().getFigureCreatorHolder();

        // register the FSFigure as a possible token representation
        fsFigureCreator = new FigureCreator() {
                public boolean canCreateFigure(ObjectAccessor token,
                                               boolean expanded) {
                    if (token instanceof ObjectAccessorImpl) {
                        Object obj = ((ObjectAccessorImpl) token).getObject();
                        return obj instanceof FeatureStructure
                               || (expanded
                                  && !(obj instanceof NetInstance
                                  || obj instanceof Value
                                  || obj instanceof String));
                    } else {
                        return false;
                    }
                }

                public Figure getTokenFigure(ObjectAccessor token,
                                             boolean expanded) {
                    assert (token instanceof ObjectAccessorImpl) : "FS-getTokenFigure called with remote token.";
                    Object obj = ((ObjectAccessorImpl) token).getObject();
                    if (obj instanceof FeatureStructure) {
                        return new FSFigure((FeatureStructure) obj, expanded);
                    } else {
                        return new FSFigure(obj);
                    }
                }
            };
        fc.registerCreator(fsFigureCreator);

        FormalismPlugin.getCurrent()
                       .addCompilerFactory(XFSNET_COMPILER, new XFSNetCompiler());
        FormalismGuiPlugin.getCurrent()
                          .addGuiConfigurator(XFSNET_COMPILER,
                                              new FSGuiConfigurator());

        if (getProperties().getBoolProperty(FS_EXPERIMENTAL_COMPILERS_PROP)) {
            logger.info("Enabling experimental FSNet compilers.");

            FormalismPlugin.getCurrent()
                           .addCompilerFactory(FSNET_COMPILER,
                                               new FSNetCompiler());
            FormalismGuiPlugin.getCurrent()
                              .addGuiConfigurator(FSNET_COMPILER,
                                                  new FSGuiConfigurator());

            FormalismPlugin.getCurrent()
                           .addCompilerFactory(FSNET_WOC_COMPILER,
                                               new FSNetWithoutConceptsCompiler());
            FormalismGuiPlugin.getCurrent()
                              .addGuiConfigurator(FSNET_WOC_COMPILER,
                                                  new FSGuiConfigurator());

            FormalismPlugin.getCurrent()
                           .addCompilerFactory(EFSNET_COMPILER,
                                               new EFSNetCompiler());
            FormalismGuiPlugin.getCurrent()
                              .addGuiConfigurator(EFSNET_COMPILER,
                                                  new EFSGuiConfigurator());
        } else {
            logger.debug("Experimental FSNet compilers disabled.");
        }


        // fsTextFigureCreator = new FSTextFigureCreator();
        // fc.registerTextFigureCreator(fsTextFigureCreator);
    }

    /**
     * Unregisters the GUI extensions of the FS plug-in.
     *
     * @return <code>true</code> if the cleanup was successful.
     **/
    public final boolean cleanup() {
        FormalismGuiPlugin.getCurrent().removeGuiConfigurator(FSNET_COMPILER);
        FormalismPlugin.getCurrent().removeCompilerFactory(FSNET_COMPILER);
        FormalismGuiPlugin.getCurrent().removeGuiConfigurator(FSNET_WOC_COMPILER);
        FormalismPlugin.getCurrent().removeCompilerFactory(FSNET_WOC_COMPILER);
        FormalismGuiPlugin.getCurrent().removeGuiConfigurator(XFSNET_COMPILER);
        FormalismPlugin.getCurrent().removeCompilerFactory(XFSNET_COMPILER);
        FormalismGuiPlugin.getCurrent().removeGuiConfigurator(EFSNET_COMPILER);
        FormalismPlugin.getCurrent().removeCompilerFactory(EFSNET_COMPILER);
        try {
            FigureCreatorHolder fc = GuiPlugin.getCurrent()
                                              .getFigureCreatorHolder();
            if (fsFigureCreator != null) {
                fc.unregisterCreator(fsFigureCreator);
                fsFigureCreator = null;
            }
            if (fsTextFigureCreator != null) {
                fc.unregisterCreator(fsTextFigureCreator);
                fsTextFigureCreator = null;
            }
        } catch (NullPointerException e) {
            logger.error("Could not cleanup: " + e, e);
        }
        return true;
    }

    public static FSPlugin getCurrent() {
        Iterator<IPlugin> plugins = PluginManager.getInstance()
                                                 .getPluginsProviding("de.renew.fs")
                                                 .iterator();
        while (plugins.hasNext()) {
            IPlugin plugin = plugins.next();
            if (plugin instanceof FSPlugin) {
                return (FSPlugin) plugin;
            }
        }
        return null;
    }
}