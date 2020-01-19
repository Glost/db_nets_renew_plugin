package de.renew.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


/**
 *
 * @author Martin Kettembeil, Mirko Heger
 *
 * java-class for an ant-task to create the documentation of an agentsystem found in a given
 * folder as a LaTex-file.
 *
 */
public class CreateDocumentationTask extends Task {
    private File destdir = null; // documentation-directory 
    private File agentSystemDir = null; // directory of the agentsystem. there have to be at least
                                        // the two directories "agents" and "interactions"
    private File laTexFile = null;
    private FileWriter laTexWriter;

    public CreateDocumentationTask() {
        super();
    }

    public void setDestdir(File dest) {
        this.destdir = dest;
    }


    /**
     * Creates a LaTex-file for documentation in the documentationfolder and collects
     * information about the agents, interactions, some other nets, and an
     * ontology-javadoc-file which it writes into the LaTex-file
     * @param args
     */
    public void execute() throws BuildException {
        super.execute();

        this.agentSystemDir = new File(destdir.getParentFile().getParent(),
                                       "//src//de//renew//agent//wfms");
        this.laTexFile = new File(destdir, "documentation.tex"); //create LaTex-document in documentationFolder
        System.out.println(laTexFile.getPath());

        try {
            laTexWriter = new FileWriter(laTexFile); //opens LaTex-File  
            laTexWriter.write("\\documentclass[a4paper]{article} \n"
                              + "\\usepackage{ngerman} \n"
                              + "\\usepackage{ifthen} \n"
                              + "\\usepackage{alltt} \n"
                              + "\\usepackage{graphicx} \n \n"
                              + "\\usepackage{listings} \n \n"
                              + "\\newenvironment{netdocDocument} {} {} \n"
                              + "\\newenvironment{netdocDescription} {\\begin{alltt}} {\\end{alltt}} \n"
                              + "\\newcommand*{ \\netdocDrawingName} [1] {} \n"
                              + "\\newcommand*{ \\netdocDrawingPackage} [1] {} \n"
                              + "\\newcommand*{ \\netdocTitle} [1] {#1  \\\\} \n"
                              + "\\newcommand*{ \\netdocAuthor} [1] {#1 \\\\} \n"
                              + "\\newcommand*{ \\netdocDateCreation} [1] {#1 \\\\} \n"
                              + "\\newcommand*{ \\netdocDateLastModified} [1] {#1 \\\\} \n \n \n \n"
                              + "\\begin{document} \n \n"
                              + "\\lstset{breaklines} \n \n"
                              + "\\tableofcontents{} \n \n" + "\\newpage \n \n"); //writes LaTex-head into the file

        } catch (IOException e) {
            System.out.println("IOException: problem while opening or writing in latexfile with filewriter");
        }
        try {
            createAgentChapter(agentSystemDir); //adds the agent-documentation
            createInteractionChapter(agentSystemDir); //adds the interaction-documentation
            createSpecialChapter(agentSystemDir); //adds documentation for other nets
            laTexWriter.write("\\section{Verweis auf JavaDoc-Datei der Ontologie = index.html} \n \n"); //adds ducumentation for the ontology
            laTexWriter.write("\n \n \n \\end{document}");
            laTexWriter.close(); //close LaTex document;					
        } catch (IOException e) {
            System.out.println("IOException: Error while creating LaTex-File");
        }
    }


    /**
     * First the method lists all agent-directories and visits one after another.
     * In those agent-directories it first searches for a *.wis-file and copies it
     * to the LaTex-document. Than it searches for the net-files and their corresponding
     * doctex-files and adds them to the laTex-document as well.
     *
     * @param agentSystemFolder
     */
    private void createAgentChapter(File agentSystemFolder)
            throws IOException {
        laTexWriter.write("\\section{Agenten} \n \n"); //start of new LaTex-section

        File agentFolder = new File(agentSystemFolder, "agents"); //sets agentFolder

        File[] agentDirs = agentFolder.listFiles(); //creates an array of all agent-directories inside the agentFolder

        for (int i = 0; i < agentDirs.length; i++) //visits all agent-directories one after another;
         {
            if (agentDirs[i].isDirectory() && agentDirs[i].getName() != "CVS") {
                File currentAgentDirectory = agentDirs[i]; //sets our current agent-directory
                File[] agentFiles = currentAgentDirectory.listFiles(); //creates list of all files in the current agent-directory
                try {
                    docuWisFile(agentFiles, currentAgentDirectory); //documents wis-file in the LaTex-file
                    docuRnwFiles(agentFiles); //documents rnw-files and their corresponding doctex-files
                } catch (FileNotFoundException e) {
                    System.out.println("can not find file");
                }
            }
        }
    }


    /**
     * First the method lists all interaction-directories and visits one after another.
     * In those interaction-directories it first searches for a *.aip-file and copies it
     * to the LaTex-document. Than it searches for the net-files and there corresponding
     * doctex-files and adds them to the laTex-document as well.
     */
    private void createInteractionChapter(File agentSystemFolder)
            throws IOException {
        laTexWriter.write("\\section{Interaktionen} \n \n"); //start of new LaTex-section


        File interactionFolder = new File(agentSystemFolder, "interactions"); //sets interactionFolder
        File[] interactionDirs = interactionFolder.listFiles(); //creates list of all interaction-directories inside the interactionFolder
        for (int i = 0; i < interactionDirs.length; i++) //visits all interaction-directories one after another;
         {
            if (interactionDirs[i].isDirectory()
                        && interactionDirs[i].getName() != "CVS") {
                File currentInteractionDirectory = interactionDirs[i]; //sets our current interaction-directory
                File[] interactionFiles = currentInteractionDirectory.listFiles(); //creates list of all files in the current interaction-directory

                docuAipFile(interactionFiles, currentInteractionDirectory); //documents aip-file and its corresponding doctex-file
                docuRnwFiles(interactionFiles); //documents rnw-files and their corresponding doctex-files

            }
        }
    }


    /**
     * The method searches in all directories of the agentsystem except for the agents- and
     * interctions-directory for rnw-files. If it finds some, it adds them to the LaTex-file.
     * @param agentSystemFolder
     */
    private void createSpecialChapter(File agentSystemFolder)
            throws IOException {
        laTexWriter.write("\\section{Weitere Netze} \n \n"
                          + "\\subsection{Netze} \n \n"); //start of new LaTex-section


        File[] agentSystemDirs = agentSystemFolder.listFiles(); //creates list of all directories inside the agentSystemFolder

        for (int i = 0; i < agentSystemDirs.length; i++) //visits all interaction-directories one after another;
         {
            if ((agentSystemDirs[i].getName() != ("agents"))
                        && (agentSystemDirs[i].getName() != "interactions")
                        && (agentSystemDirs[i].isDirectory()
                                   && agentSystemDirs[i].getName() != "CVS")) {
                File currentDirectory = agentSystemDirs[i]; //sets our current directory
                File[] agentSystemFiles = currentDirectory.listFiles(); //creates list of all files in the current directory
                docuRnwFiles(agentSystemFiles); //documents rnw-files and their corresponding doctex-files
            }
        }
    }


    /**
     * Creates a copy of inputFile called outputFile
     * @param inputFile
     * @param outputFile
     * @throws IOException
     */
    private void fileCopy(File inputFile, File outputFile) {
        try {
            FileReader in = new FileReader(inputFile); //opens inputFile
            FileWriter out = new FileWriter(outputFile); //opens outputFile
            int c;
            while ((c = in.read()) != -1) {
                out.write(c); //the end of the file
            }
            in.close();
            out.close();
        } catch (IOException e) {
            System.out.println("file copy error");
        }
    }


    /**
     * searches for "agent.wis"-file in the folder and adds it to the LaTex-file
     * @param agentFiles
     */
    private void docuWisFile(File[] agentFiles, File directory)
            throws FileNotFoundException, IOException {
        for (int j = 0; j < agentFiles.length; j++) // visits all files in the directory
         {
            if (agentFiles[j].getName()
                                     .equalsIgnoreCase(directory.getName()
                                                               + ".wis")) // searches for file of the form "agent.wis"
             {
                String subsectionName = agentFiles[j].getName()
                                                     .replaceFirst(".wis", "");

                //               String destdirPath=destdir.getPath().replace("eclipse-Workspace", "eclipse\-Workspace");
                laTexWriter.write("\\subsection{" + subsectionName + "} \n \n");
                laTexWriter.write("\\subsubsection{" + agentFiles[j].getName()
                                  + "} \n" + "\\begin{lstlisting} \n");


                /**                                        "\\input{" + agentFiles[j].getName() + "} \n" +
                                                        "\\end{alltt} \n");

                                File wisFile = agentFiles[j];                                          // sets doctexFile if doctex-file was found
                                File destWisFile = new File(destdir, wisFile.getName());
                                fileCopy(wisFile, destWisFile);

                */
                File wisFile = agentFiles[j]; // sets wisFile if "agent.wis"-file was found
                File destWisFile = new File(destdir, wisFile.getName());
                fileCopy(wisFile, destWisFile); // copies file to the documentation-folder
                FileReader wisFileReader = new FileReader(wisFile); // opens "agent.wis"

                int wisFileCharakter;
                try {
                    while ((wisFileCharakter = wisFileReader.read()) != -1) // writes content of the wis-file into the LaTex-file
                     {
                        laTexWriter.write((char) wisFileCharakter);
                    }
                } catch (IOException e) {
                    System.out.println("Error while working on files");
                }
                try {
                    laTexWriter.flush();
                    wisFileReader.close();
                } catch (IOException e) {
                    System.out.println("unable to close wisFileReader");
                }
                laTexWriter.write("\\end{lstlisting} \n");
                break;


            }
        }
    }


    /**
     * Searches for "interaction.aip"-file and its corresponding *.doctex-file in the folder and
     * adds them to the LaTex-file.
     * @param interactionFiles
     * @param directory
     */
    private void docuAipFile(File[] interactionFiles, File directory)
            throws IOException {
        for (int j = 0; j < interactionFiles.length; j++) // visits all files in the directory
         {
            if (interactionFiles[j].getName()
                                           .equalsIgnoreCase(directory.getName()
                                                                     + ".aip")) // searches for file of the form "interaction.aip"
             {
                String latexName = interactionFiles[j].getName()
                                                      .replaceFirst(".aip", "");
                laTexWriter.write("\\subsection{" + latexName + "} \n \n");
                laTexWriter.write("\\subsubsection{" + latexName + "} \n \n"
                                  + "\\includegraphics[width=\\textwidth, height=\\textheight]{"
                                  + latexName + ".eps} \n");
                laTexWriter.write("\\caption{" + latexName + "} \n"); // writes image-link into the LaTex-file


                File currentFile = interactionFiles[j]; // sets currentFile if "interaction.aip"-file was found
                File destFile = new File(destdir, currentFile.getName());
                fileCopy(currentFile, destFile); // copies file to the documentation-folder
                String aipName = new String(currentFile.getName()
                                                       .substring(0,
                                                                  currentFile.getName()
                                                                             .length()
                                                                  - 4));

                /**                        try{
                                laTexWriter.write(aipName + ".eps");                // writes image-link into the LaTex-file
                                } catch (IOException e){
                                    System.out.println("unable to write into LaTex-file");
                                }
                */
                for (int k = 0; k < interactionFiles.length; k++) // searches for corresponding doctex-file
                 {
                    if (interactionFiles[k].getName().equals(aipName
                                                                     + ".doctex")) {
                        File doctexFile = interactionFiles[k]; // sets doctexFile if doctex-file was found
                        File destDoctexFile = new File(destdir,
                                                       doctexFile.getName());
                        fileCopy(doctexFile, destDoctexFile); // copies file to the documentation-folder
                        try {
                            laTexWriter.write("\\input {"
                                              + doctexFile.getName() + "} \n");
                        } catch (IOException e) {
                            System.out.println("");
                        }
                        break;
                    }
                }
                break;
            }
        }
        try {
            laTexWriter.flush();
        } catch (IOException e) {
            System.out.println("OVERFLUSH!!!");
        }
    }


    /**
     * Searches for *.rnw-files and their corresponding *.doctex-files in the folder and
     * adds them to the LaTex-file.
     *
     * @param agentFiles
     */


    //NOTICEthrows
    private void docuRnwFiles(File[] files) throws IOException {
        for (int j = 0; j < files.length; j++) // visits all files in the directory
         {
            if (files[j].getName().endsWith(".rnw")) // searches for file of the form *.rnw
             {
                File currentFile = files[j]; // sets currentFile if a *.rnw-file was found
                File destFile = new File(destdir, currentFile.getName());
                fileCopy(currentFile, destFile); // copies file to the documentation-folder
                String netName = new String(currentFile.getName()
                                                       .substring(0,
                                                                  currentFile.getName()
                                                                             .length()
                                                                  - 4));

                try {
                    String netLatexName = netName.replace("_", "\\_");
                    laTexWriter.write("\\subsubsection{" + netLatexName
                                      + ".rnw} \n"
                                      + "\\includegraphics[angle=90, width=\\textwidth, height=19cm]{"
                                      + netName + ".eps} \n");
                    laTexWriter.write("\\caption{" + netLatexName + "} \n"); // writes image-link into the LaTex-file
                } catch (IOException e) {
                    System.out.println("unable to write into LaTex-file");
                }

                for (int k = 0; k < files.length; k++) // searches for corresponding doctex-file
                 {
                    if (files[k].getName().equals(netName + ".doctex")) {
                        File doctexFile = files[k]; // sets doctexFile if doctex-file was found
                        File destDoctexFile = new File(destdir,
                                                       doctexFile.getName());
                        fileCopy(doctexFile, destDoctexFile); // copies file to the documentation-folder
                        try {
                            laTexWriter.write("\\input {"
                                              + doctexFile.getName() + "} \n");
                        } catch (IOException e) {
                            System.out.println("IOException: error while writing doctex-file-link into latex-file");
                        }
                        break;
                    }
                }
            }
        }
        try {
            laTexWriter.flush();
        } catch (IOException e) {
            System.out.println("OVERFLUSH!!!");
        }
    }
}