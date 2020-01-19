package de.renew.gui;

import de.renew.application.SimulatorPlugin;

import de.renew.formalism.FormalismChangeListener;
import de.renew.formalism.FormalismPlugin;

import de.renew.shadow.ShadowCompilerFactory;

import java.awt.EventQueue;


/**
 * @author joern
 *
 */
public class ModeReplacement implements FormalismChangeListener {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ModeReplacement.class);
    private static ModeReplacement _instance;
    private CPNSimulation _simulation;
    CPNDrawingLoader _drawingLoader;
    GuiFinder _finder;

    //RenewMode _mode;
    //ShadowCompiler _compiler;


    /**
     * Constructor for ModeReplacement.
     */
    private ModeReplacement() {
        super();
    }

    public CPNDrawingLoader getDrawingLoader() {
        if (_drawingLoader == null) {
            setDrawingLoader(new CPNDrawingLoader());
        }
        return _drawingLoader;
    }

    public static synchronized ModeReplacement getInstance() {
        if (_instance == null) {
            _instance = new ModeReplacement();
        }
        return _instance;
    }

    static synchronized void killInstance() {
        ModeReplacement oldInstance = _instance;
        _instance = null;
        if (oldInstance != null) {
            oldInstance.setDrawingLoader(null);
            oldInstance.getSimulation().dispose();
            oldInstance.setSimulation(null);
        }
    }

    //    public RenewMode getMode() {
    //        return _mode;
    //    }
    public CPNSimulation getSimulation() {
        if (_simulation == null) {
            _simulation = new CPNSimulation(false, getDrawingLoader());
        }
        return _simulation;
    }

    public void setSimulation(CPNSimulation sim) {
        _simulation = sim;
    }

    public void setDrawingLoader(CPNDrawingLoader loader) {
        _drawingLoader = loader;
        if (_finder != null) {
            SimulatorPlugin.getCurrent().removeDefaultNetFinder(_finder);
            _finder = null;
        }
        if (_drawingLoader != null) {
            _finder = new GuiFinder(_drawingLoader);
            SimulatorPlugin.getCurrent().registerDefaultNetFinder(_finder);
        }
    }

    public void formalismChanged(String newFormalism, Object st, int action) {
        if (action == FormalismChangeListener.CHOOSE) {
            if (_simulation != null && _simulation.isSimulationActive()) {
                logger.error("cannot change compiler: active simulation detected.");
            } else {
                if (_simulation != null && _simulation.getNetSystem() != null) {
                    FormalismPlugin fp = FormalismPlugin.getCurrent();
                    _simulation.getNetSystem()
                               .setDefaultCompilerFactory(fp
                        .getCompilerFactoryByName(fp.getCompiler()));
                }
                final CPNApplication app = GuiPlugin.getCurrent().getGui();
                if (app != null) {
                    EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                app.syntaxCheck();
                            }
                        });
                }
            }
        }
    }

    public ShadowCompilerFactory getDefaultCompilerFactory() {
        FormalismPlugin fp = FormalismPlugin.getCurrent();
        String currentCompiler = fp.getCompiler();
        if (currentCompiler != null) {
            return fp.getCompilerFactoryByName(currentCompiler);
        }
        return null;
    }
}