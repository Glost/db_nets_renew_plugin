package de.renew.plugin.command;

import de.renew.plugin.PluginManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This command reads the file given in the first argument
 * and interprets each line as a command to be executed by the PluginManager.
 *
 * @author J&ouml;rn Schumacher
 */
public class ScriptCommand implements CLCommand {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ScriptCommand.class);

    /**
     * Starts the execution of the given script file.
     * The file is specified via <code>args</code>, the output of all
     * scripted commands goes to <code>response</code>.
     *
     * @param args {@inheritDoc}
     * @param response {@inheritDoc}
     **/
    @Override
    public void execute(String[] args, PrintStream response) {
        if (args.length > 1) {
            response.println("ScriptCommand: more than one parameter given: using only "
                             + args[0]);
        }
        Collection<String> lines = readFile(args[0], response);
        Collection<CommandArgumentTuple> commands = createCommands(lines,
                                                                   response);
        Iterator<CommandArgumentTuple> commandIterator = commands.iterator();
        while (commandIterator.hasNext()) {
            try {
                CommandArgumentTuple command = commandIterator.next();

                command.execute(response);
            } catch (RuntimeException e) {
                logger.error("PluginManager.PromptThread: an exeption occurred: "
                             + e);
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * @see de.renew.plugin.command.CLCommand#getDescription()
     */
    @Override
    public String getDescription() {
        return "reads an input file and interprets every line as a PluginManager command";
    }

    private Collection<String> readFile(String filename, PrintStream response) {
        response.println("ScriptCommand: Try to load file " + filename);
        Vector<String> lines = new Vector<String>();
        File f = new File(filename);
        String line = "";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            while (line != null) {
                line = reader.readLine();
                if (line != null) {
                    lines.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            response.println("ScriptCommand: file " + filename + " not found.");
        } catch (IOException e) {
            logger.error("ScriptCommand: exception when reading " + filename
                         + ": ", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lines;
    }

    private Collection<CommandArgumentTuple> createCommands(Collection<String> toConvertCollection,
                                                            PrintStream response) {
        Map<String, CLCommand> commands = PluginManager.getInstance()
                                                       .getCLCommands();
        Vector<CommandArgumentTuple> result = new Vector<CommandArgumentTuple>();
        Iterator<String> toConvertIterator = toConvertCollection.iterator();

        boolean blockCommentActive = false;

        while (toConvertIterator.hasNext()) {
            String toConvert = toConvertIterator.next();
            if (!blockCommentActive && toConvert.trim().startsWith("/*")) {
                logger.debug("Block comment started");
                blockCommentActive = true;
            }
            if (blockCommentActive
                        && (toConvert.trim().endsWith("*/")
                                   || toConvert.trim().equals("*/"))) {
                logger.debug("Block comment ended");
                blockCommentActive = false;
                continue;
            }

            logger.debug("CONVERT TO COMMAND " + toConvert);

            List<String> split = splitString(toConvert);
            if (toConvert.trim().length() == 0 || blockCommentActive
                        || (split.size() == 0)
                        || (split.get(0).trim().startsWith("#"))) {
                // So this is an empty line or a comment.
                // Ignore it.
            } else {
                CLCommand c = commands.get(split.get(0));
                if (c == null) {
                    response.println("ScriptCommand: unknown command: "
                                     + split.get(0));
                } else {
                    String[] nc = new String[split.size() - 1];
                    for (int i = 0; i < nc.length; i++) {
                        nc[i] = split.get(i + 1);
                    }
                    CommandArgumentTuple cat = new CommandArgumentTuple(c, nc);
                    result.add(cat);
                }
            }
        }
        return result;
    }

    protected List<String> splitString(String s) {
        List<String> list = new ArrayList<String>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(s);
        while (m.find()) {
            list.add(m.group(1).replace("\"", ""));
        }
        return list;
    }

    private class CommandArgumentTuple {
        private CLCommand _command;
        private String[] _arguments;

        public CommandArgumentTuple(CLCommand command, String[] args) {
            _command = command;
            _arguments = args;
        }

        public void execute(PrintStream response) {
            _command.execute(_arguments, response);
        }
    }

    /**
     * @see de.renew.plugin.command.CLCommand#getArguments()
     */
    @Override
    public String getArguments() {
        return "fileNames";
    }
}