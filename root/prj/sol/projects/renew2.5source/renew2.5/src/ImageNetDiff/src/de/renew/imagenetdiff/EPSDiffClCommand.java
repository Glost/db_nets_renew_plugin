/**
 *
 */
package de.renew.imagenetdiff;



/**
 * @author Lawrence Cabac
 *
 */
public class EPSDiffClCommand extends AbstractDiffClCommand {
    private static final String EPSDIFF_COMMAND = "epsdiff";

    /**
     *
     */
    public EPSDiffClCommand() {
        super();
        diffCommand = new EPSDiffCommand();
        COMMAND = EPSDIFF_COMMAND;
    }

    /* (non-Javadoc)
     * @see de.renew.plugin.command.CLCommand#getDescription()
     */
    public String getDescription() {
        return "Synopsis: " + getName()
               + " file1 [file2]. Constructs a diff image.\n"
               + "                if only one argument is given " + getName()
               + " assumes diff against \n"
               + "                svn base file (in <filepath>/.svn/text-base/<filename>.svn-base).\n"
               + "                Command produces EPS files of drawings in temp folder.";

    }
}