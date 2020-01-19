package de.renew.mxml;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.Date;


/**
 *        This class helps to build the diferent XML blocks of the  {@link MXMLLayout}
 * @author Jorge Sangines(8sangine)
 */
public class MXMLHelper {
    private StringBuilder attributes;
    private StringBuilder data;
    private Date date;
    private String originator;
    private SimpleDateFormat form;
    private String defaultHeader;
    private String processInstanceDefaultHeader;
    private String auditTrailDefaultHeader;

    /**
     * Constructor for the instantiation of several {@link StringBuffer}s to build the xml blocks.
     * <p>The default headers with the namespaces of the MXML format and the first blocks for the different
     * layers of the MXML Format are also hard coded here
     */
    public MXMLHelper() {
        defaultHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
                        + "<!-- MXML version 1.0 -->"
                        + "<!-- This is a process event log created to be analyzed by ProM. When analyzing please discard the events of type \"ignore\"-->"
                        + "<!-- ProM is the process mining framework. It can be freely obtained at http://www.processmining.org/. -->"
                        + "<WorkflowLog xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation="
                        + "\"http://is.tm.tue.nl/research/processmining/WorkflowLog.xsd\" description=\"PAOSE Workflow log\">"
                        + "<Source program=\"Renew Mulan\"/>";
        processInstanceDefaultHeader = defaultHeader
                                       + "<Process id=\"DEFAULT\" description=\"Default process\">";
        auditTrailDefaultHeader = processInstanceDefaultHeader
                                  + "<ProcessInstance id=\"DEFAULT\" description=\"Default process instance\">";
        attributes = new StringBuilder();
        data = new StringBuilder();
        data.append("<Data>");
        data.append("</Data>");
        date = new Date();
        MXMLCommandCenter var = MXMLCommandCenter.getInstance();
        originator = var.getUserName();
        form = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    }

    /**
     * Passes an attribute name and its value for the data block of the MXML format
     * @param name the name of the data attribute
     * @param value the value of the data attribute
     */
    public void addAttribute(String name, String value) {
        String temp = "<Attribute name=\"" + name + "\">" + value
                      + "</Attribute>";
        attributes.append(temp);
    }

    /**
     * Passes arguments to generate and return an <code>AuditTrailEntry</code>
     *  for the MXML format.
     *  <p>If the log file is empty it also adds the <code>auditTrailDefaultHeader</code>
     *  to the beginning of the string
     * @param element the name of the workflow model element of the <code>AuditTrailEntry</code>
     * @param type the event type of the workflow model element of the <code>AuditTrailEntry</code>
     * @return the {@link String} with the <code>AuditTrailEntry</code> containing
     * the workflow model element and if given its data
     */
    public String generateAudiTrail(String element, String type) {
        BufferedReader br = null;

        try {
            String sCurrentLine;

            br = new BufferedReader(new FileReader(MXMLCommandCenter.FILE));
            sCurrentLine = br.readLine();

            StringBuilder trail = new StringBuilder();
            trail.append("<AuditTrailEntry>");
            if (attributes.length() > 0) {
                data.insert(6, attributes);
                trail.append(data);
            }
            trail.append("<WorkflowModelElement>" + element
                         + "</WorkflowModelElement>");
            trail.append("<EventType>" + type + "</EventType>");
            String st = form.format(date);
            trail.append("<Timestamp>" + st + "</Timestamp>");
            trail.append("<Originator>" + originator + "</Originator>");
            trail.append("</AuditTrailEntry>\n");
            if (sCurrentLine == null) {
                trail.insert(0, auditTrailDefaultHeader);
                System.out.println("INFO: New log file generated");
            }
            br.close();
            return trail.toString();

        } catch (IOException e) {
            MXMLCommandCenter.lockEnvironment();
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                // ex.printStackTrace();
            }
            return "";
        }
    }

    /**
     * Closes the opening tags of the previous process instance and opens a
     *  new instance with the given arguments in the MXML format.
     *  <p>If the log file is empty it will add the <code>processInstanceDefaultHeader</code>
     *   to the beginning of the string
     * @param id
     * @param prop
     * @return the {@link String} with the <code>ProcessInstance</code> containing
     * its identifiers and if given its data
     */
    public String generateProcessInstance(String id, String prop) {
        BufferedReader br = null;

        try {
            String ret = "";
            String sCurrentLine;

            br = new BufferedReader(new FileReader(MXMLCommandCenter.FILE));
            sCurrentLine = br.readLine();
            if (sCurrentLine == null) {
                ret = processInstanceDefaultHeader + "<ProcessInstance id=\"";
                System.out.println("INFO: New log file generated");
            } else {
                ret = "</ProcessInstance><ProcessInstance id=\"";
                System.out.println("INFO: Log file updated");
            }

            ret = ret + id + "\" description=\"" + prop + "\">";
            if (attributes.length() > 0) {
                data.insert(6, attributes);
                ret = ret + data;
            }
            br.close();
            return ret;

        } catch (IOException e) {
            MXMLCommandCenter.lockEnvironment();
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                // ex.printStackTrace();
            }
            return "";
        }
    }

    /**
     * Closes the opening tags of the previous process and opens a
     *  new instance with the given arguments in the MXML format. This method is still in an experimental
     *   state and its use its discouraged
     *  <p>If the log file is empty it will add the <code>defaultHeader</code>
     *   to the beginning of the string
     * @param id
     * @param prop
     * @return the {@link String} with the <code>Process</code> containing
     * its identifiers and if given its data
     */
    public String generateProcess(String id, String prop) {
        BufferedReader br = null;

        try {
            String ret = "";
            String sCurrentLine;

            br = new BufferedReader(new FileReader(MXMLCommandCenter.FILE));
            sCurrentLine = br.readLine();
            if (sCurrentLine == null) {
                ret = defaultHeader + "<ProcessInstance id=\"";
                System.out.println("INFO: New log file generated");
            } else {
                ret = "</Process><Process id=\"";
                System.out.println("INFO: Log file updated");
            }

            ret = ret + id + "\" description=\"" + prop + "\">";
            if (attributes.length() > 0) {
                data.insert(6, attributes);
                ret = ret + data;
            }
            br.close();
            return ret;

        } catch (IOException e) {
            MXMLCommandCenter.lockEnvironment();
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                // ex.printStackTrace();
            }
            return "";
        }
    }
}