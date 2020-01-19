/**
 *
 */
package de.renew.imagenetdiff;



/**
 * @author Lawrence Cabac
 *
 */
public class ImageNetDiffClCommand extends AbstractDiffClCommand {
    private static final String DIFF_COMMAND = "diff";

    /**
     *
     */
    public ImageNetDiffClCommand() {
        super();
        diffCommand = new PNGDiffCommand();
        COMMAND = DIFF_COMMAND;
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
               + "                Command produces PNG files of drawings in temp folder.";

    }
}