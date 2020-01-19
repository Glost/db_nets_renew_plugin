package de.renew.call;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import java.util.StringTokenizer;


public class StubCompiler {
    private final static String AUTO_COMMENT = "// This file was automatically generated. Do not modify.";
    private File _toDir;

    /**
     * @see #StubCompiler(File) . File is set to <code>null</code>.
     */
    public StubCompiler() {
        this(null);
    }

    /**
     * Constructs the StubCompiler with a target directory, in which the
     * java files will be written.
     *
     * @param toDir the root directory for the generated code.
     *              If <code>null</code>, the source path will be used.
     */
    public StubCompiler(File toDir) {
        _toDir = toDir;
    }

    /**
     * generates a .java file from a .stub file.
     * target file is calculated from constructor argument and
     * package path of .stub file.
     * @param source the .stub file
     * @return File location of the target file
     * @throws ParseException an error occured while Parsing the stub file
     * @throws IOException an error occured while reading or writing a file
     */
    public File compileStub(File source) throws ParseException, IOException {
        FileReader sourceReader = null;
        sourceReader = new FileReader(source);
        StubParser parser = new StubParser(sourceReader);
        parser.StubFile();

        File target = getTarget(source, parser.getPackage());
        if (!canWrite(target)) {
            throw new ParseException("not allowed to edit file");
        }

        Writer w = new BufferedWriter(new FileWriter(target));
        w.write(AUTO_COMMENT + "\n");
        w.write(parser.getOutput());
        w.close();

        return target;
    }

    private boolean canWrite(File targetFile) throws IOException {
        if (!targetFile.isFile()) {
            return true;
        }
        BufferedReader targetReader = new BufferedReader(new FileReader(targetFile));
        String firstLine = targetReader.readLine();
        targetReader.close();
        if (firstLine == null || firstLine.startsWith(AUTO_COMMENT)) {
            return true;
        }
        return false;
    }

    private File getTarget(File source, String pckg) throws IOException {
        StringBuffer path = new StringBuffer();
        if (_toDir == null) {
            String parent = source.getParent();
            if (parent != null) {
                path.append(parent + File.separator);
            } else {
                path.append("." + File.separator);
            }
        } else {
            path.append(_toDir + File.separator);
            StringTokenizer st = new StringTokenizer(pckg, ".");
            for (; st.hasMoreTokens();) {
                path.append(st.nextToken() + File.separator);
            }
        }

        // Filename
        String name = source.getName();
        String genName = name.substring(0, name.lastIndexOf('.')) + ".java";
        path.append(genName);
        File result = new File(path.toString());
        result.getParentFile().mkdirs();

        if (!(result.createNewFile() || result.canWrite())) {
            throw new IOException("Could not specify target File "
                                  + "of source file '" + source + "'.\n"
                                  + "Estimated '" + result
                                  + "' was errorneous.");
        }
        return result;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            usage();
            System.exit(1);
        }

        File toDir = null;
        StubCompiler c = new StubCompiler(toDir);

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-d")) {
                i++;
                toDir = new File(args[i]);
                if (!toDir.isDirectory()) {
                    System.err.println("\n\nSpecified target directory '"
                                       + toDir + "' does not exist.\n\n");
                    System.exit(1);
                }
                c = new StubCompiler(toDir);
                continue;
            }

            File sourceFile = new File(args[i]);
            if (!sourceFile.isFile()) {
                System.err.println("Source file '" + sourceFile
                                   + "'could not be found.");
                System.exit(1);
            }

            if (!sourceFile.canRead()) {
                System.err.println("Cannot read source file '" + sourceFile
                                   + "'." + "Ignoring this argument.");
                continue;
            }
            try {
                File targetFile = c.compileStub(sourceFile);
                System.err.println("Target file " + targetFile + " generated.");
            } catch (ParseException e) {
                System.err.println("An error occurred during parsing the stub file.");
                System.err.println(e.getMessage());
            } catch (IOException e) {
                System.err.println("An error occurred while reading source file or "
                                   + "writing the target file:");
                System.err.println(e.getMessage());
            }
        }
    }

    private static void usage() {
        System.err.println("Renew Stub Compiler\n" + "\n"
                           + "Generates .java files from .stub files\n"
                           + "All .stub files provided as arguments to this command are\n"
                           + "compiled to .java files. These can be used to unify with \n"
                           + "synchronous channels out of java-Code.\n" + "\n"
                           + "Usage:\n"
                           + "java de.renew.call.StubCompiler {-d <todir>} {stub-files}...\n"
                           + "\n" + "-d <todir>\n"
                           + "        Directory in which java Files are generated.\n"
                           + "        Like java files, they are created into their package path.\n"
                           + "        If not specified, files will be generated in the same path.\n"
                           + "        \n" + "{stub-files}...\n"
                           + "        some .stub Files that shall be compiled.\n"
                           + "        Existing Files will be overwritten unless the \n"
                           + "        \" do not modify \" warning has been removed.\n"
                           + "\n");
    }
}