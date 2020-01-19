/**
 *
 */
package de.renew.pd.commands;

import CH.ifa.draw.util.Command;

import de.renew.pd.generating.PluginGenerator;
import de.renew.pd.generating.StandardPluginGenerator;


/**
 * Calls the generator for a uc diagram.
 * @author cabac
 *
 */
public class CreateApplicationStructureCommand extends Command {
    private PluginGenerator generator;

    /**
     * @param name
     */
    public CreateApplicationStructureCommand(String name,
                                             PluginGenerator generator) {
        super(name);
        this.generator = generator;
    }

    /*
     * (non-Javadoc)
     *
     * @see CH.ifa.draw.util.Command#execute()
     */
    @Override
    public void execute() {
        this.generator.generate();
    }

    /* (non-Javadoc)
     *
     * @see CH.ifa.draw.util.Command#isExecutable()
     */
    @Override
    public boolean isExecutable() {
        return super.isExecutable();
    }
}