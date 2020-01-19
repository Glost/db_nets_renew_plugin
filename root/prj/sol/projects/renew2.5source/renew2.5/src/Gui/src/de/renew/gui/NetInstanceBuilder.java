package de.renew.gui;

import de.renew.application.NoSimulationException;
import de.renew.application.SimulatorPlugin;

import de.renew.formalism.FormalismPlugin;

import de.renew.shadow.ShadowCompilerFactory;
import de.renew.shadow.ShadowNetSystem;
import de.renew.shadow.SyntaxException;

import java.awt.EventQueue;


/**
 * @author Benjamin Schleinzer
 *
 * This static helper class allows to instantiate nets from arbitrary
 * net drawings (<code>CPNDrawing</code>) at runtime.
 */
public class NetInstanceBuilder {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(NetInstanceBuilder.class);

    /**
     * This method generates a new ShadowNet for the given CPN drawing and registers it with
     * the running simulation. If the name is "untitled" the request is kindly ignored. Old nets with
     * the same name are not removed from the simulation, but newly instantiated nets use the
     * new CPN drawing. The current default compiler is used to compile the net.
     *
     * @param cpnd
     *            The CPNDrawing to instantiate
     */
    public static void buildInstanceFromDrawing(CPNDrawing cpnd) {
        buildInstanceFromDrawing(cpnd,
                                 FormalismPlugin.getCurrent().getCompiler());
    }

    /**
     * This method generates a new ShadowNet for the given CPN drawing and registers it with
     * the running simulation. If the name is "untitled" the request is kindly ignored. Old nets with
     * the same name are not removed from the simulation, but newly instantiated nets use the
     * new CPN drawing. The used formalism can be specified by the formalismName parameter
     *
     * @param cpnd
     *            The CPNDrawing to instantiate
     * @param formalismName
     *            The name of the formalism as registered with the formalims plugin
     */
    public static void buildInstanceFromDrawing(CPNDrawing cpnd,
                                                String formalismName) {
        FormalismPlugin fp = FormalismPlugin.getCurrent();
        buildInstanceFromDrawing(cpnd,
                                 fp.getCompilerFactoryByName(formalismName));
    }


    /**
     * This method generates a new ShadowNet for the given CPN drawing and registers it with
     * the running simulation. If the name is "untitled" the request is kindly ignored. Old nets with
     * the same name are not removed from the simulation, but newly instantiated nets use the
     * new CPN drawing. The used formalism can be specified by the compilerFactory parameter
     *
     * @param cpnd
     *            The CPNDrawing to instantiate
     * @param compilerFactory
     *            A ShadowCompilerFactory object that will be used to compile the given net
     */
    public static void buildInstanceFromDrawing(CPNDrawing cpnd,
                                                ShadowCompilerFactory compilerFactory) {
        if (!cpnd.getName().equals("untitled")) {
            ShadowNetSystem sns = new ShadowNetSystem(compilerFactory);
            cpnd.buildShadow(sns);
            SimulatorPlugin simulatorPlugin = SimulatorPlugin.getCurrent();
            try {
                simulatorPlugin.insertNets(sns);
            } catch (NoSimulationException e) {
                logger.error("Simulation was terminated befor the new net has been initialized");
            } catch (final SyntaxException e) {
                EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            GuiPlugin.getCurrent()
                                     .processSyntaxException(FigureExceptionFactory
                                                             .createFigureException(e),
                                                             true);
                        }
                    });
            }
        }
    }
}