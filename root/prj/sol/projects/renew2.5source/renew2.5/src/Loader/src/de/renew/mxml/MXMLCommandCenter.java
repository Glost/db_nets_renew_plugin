package de.renew.mxml;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.Date;


/**
 *
 * @author Jorge Sangines
 *
 */
public class MXMLCommandCenter {
    private boolean state = false;
    private String userName = "";
    public static final String PROCESS_SEP = "`";
    public static final String AUDIT_TRAIL_SEP = "§";
    public static final String PROCESS_INST_SEP = "µ";
    public static final String DATA_SEP = "%";
    public static final String ATRIBUTE_SEP = "&";
    public static final int PROCESS = 1;
    public static final int PROCESS_INSTANCE = 2;
    public static final int AUDIT_TRAIL = 3;
    private static String separatorErrorMessage = "ERROR: one of the parameter is "
                                                  + "either null or contains one of the reserved symbols \""
                                                  + AUDIT_TRAIL_SEP + ", "
                                                  + DATA_SEP + ", "
                                                  + ATRIBUTE_SEP + ", "
                                                  + PROCESS_SEP + ", "
                                                  + PROCESS_INST_SEP + "\" in:";

//    public static final String COARSE_DESIGN_START = AUDIT_TRAIL_SEP
//                                                     + "coarse design"
//                                                     + AUDIT_TRAIL_SEP
//                                                     + "start";
//    public static final String COARSE_DESIGN_COMPLETE = AUDIT_TRAIL_SEP
//                                                        + "coarse design"
//                                                        + AUDIT_TRAIL_SEP
//                                                        + "complete";
//    public static final String INTERACTIONS_IMPL_START = AUDIT_TRAIL_SEP
//                                                         + "interaction implementation"
//                                                         + AUDIT_TRAIL_SEP
//                                                         + "start";
//    public static final String INTERACTIONS_IMPL_COMPLETE = AUDIT_TRAIL_SEP
//                                                            + "interaction implementation"
//                                                            + AUDIT_TRAIL_SEP
//                                                            + "complete";
//    public static final String ONTOLOGIE_IMPL_START = AUDIT_TRAIL_SEP
//                                                      + "ontologie implementation"
//                                                      + AUDIT_TRAIL_SEP
//                                                      + "start";
//    public static final String ONTOLOGIE_IMPL_COMPLETE = AUDIT_TRAIL_SEP
//                                                         + "ontologie implementation"
//                                                         + AUDIT_TRAIL_SEP
//                                                         + "complete";
//    public static final String ROLES_IMPL_START = AUDIT_TRAIL_SEP
//                                                  + "roles implementation"
//                                                  + AUDIT_TRAIL_SEP + "start";
//    public static final String ROLES_IMPL_COMPLETE = AUDIT_TRAIL_SEP
//                                                     + "roles implementation"
//                                                     + AUDIT_TRAIL_SEP
//                                                     + "complete";
//    public static final String TEST_START = AUDIT_TRAIL_SEP + "test"
//                                            + AUDIT_TRAIL_SEP + "start";
//    public static final String TEST_COMPLETE = AUDIT_TRAIL_SEP + "test"
//                                               + AUDIT_TRAIL_SEP + "complete";
//    public static final String INTEGRATION_START = AUDIT_TRAIL_SEP
//                                                   + "integration"
//                                                   + AUDIT_TRAIL_SEP + "start";
//    public static final String INTEGRATION_COMPLETE = AUDIT_TRAIL_SEP
//                                                      + "integration"
//                                                      + AUDIT_TRAIL_SEP
//                                                      + "complete";
//    public static final String DEPLOYMENT_START = AUDIT_TRAIL_SEP
//                                                  + "deployment"
//                                                  + AUDIT_TRAIL_SEP + "start";
//    public static final String DEPLOYMENT_COMPLETE = AUDIT_TRAIL_SEP
//                                                     + "deployment"
//                                                     + AUDIT_TRAIL_SEP
//                                                     + "complete";
//    public static final String ERROR_COMPLETE = AUDIT_TRAIL_SEP + "error"
//                                                + AUDIT_TRAIL_SEP + "complete";
//
//    // public static final String IGNORE = AUDFIRST_LEVEL_SEP + "ignore"
//    // + AUDFIRST_LEVEL_SEP + "ignore";
//    public static final String PROCINST_DEFAULT = PROCESS_INST_SEP + "DEFAULT"
//                                                  + PROCESS_INST_SEP
//                                                  + "Default process instance";
//    public static final String PROCINST_COARSEDESIGN = PROCESS_INST_SEP + "1"
//                                                       + PROCESS_INST_SEP
//                                                       + "coarse design";
//    public static final String PROCINST_ROLEIMPL = PROCESS_INST_SEP + "2"
//                                                   + PROCESS_INST_SEP
//                                                   + "role implementation";
//    public static final String PROCINST_ONTIMPL = PROCESS_INST_SEP + "3"
//                                                  + PROCESS_INST_SEP
//                                                  + "ontology implementation";
//    public static final String PROCINST_INTERIMPL = PROCESS_INST_SEP + "4"
//                                                    + PROCESS_INST_SEP
//                                                    + "interactions implementation";
//    public static final String PROCINST_INTEGRATION = PROCESS_INST_SEP + "5"
//                                                      + PROCESS_INST_SEP
//                                                      + "integration process";
//    public static final String PROCINST_TEST = PROCESS_INST_SEP + "6"
//                                               + PROCESS_INST_SEP + "testing";
//    public static final String PROCINST_PLUGDEV = PROCESS_INST_SEP + "7"
//                                                  + PROCESS_INST_SEP
//                                                  + "plugin development";
//    public static final String PROCINST_PAOSE = PROCESS_INST_SEP + "8"
//                                                + PROCESS_INST_SEP
//                                                + "complete Paose";
//    public static final String PROC_DEFAULT = PROCESS_SEP + "DEFAULT"
//                                              + PROCESS_SEP + "Default process";
    public static final String LOGGER = "de.renew.mxml.mxmllogger";
    public static final String FILE = System.getProperty("user.home")
                                      + File.separator + "renewlogs"
                                      + File.separator + "WorkflowLog.txt";
    public static final String FATAL = "F";
    private static boolean lock = false;

    private MXMLCommandCenter() {
    }

    private static class MXMLHolder {
        public static final MXMLCommandCenter INSTANCE = new MXMLCommandCenter();
    }

    public static MXMLCommandCenter getInstance() {
        return MXMLHolder.INSTANCE;
    }

    public boolean isOn() {
        return state;
    }

    public void changeState(boolean state) {
        if (!MXMLCommandCenter.isLocked()) {
            this.state = state;
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public static String setNumberOfAttributes(int number) {
        return number + "";
    }

    public static String setAttribute(String name, String param,
                                      boolean isFirstAttr, boolean isLastAttr) {
        String ret = "";
        if (containsSeparator(name) || containsSeparator(param)) {
            System.err.println(separatorErrorMessage);
            new Throwable().printStackTrace();
        } else {
            if (isFirstAttr) {
                ret = AUDIT_TRAIL_SEP;
            }

            ret = ret + name + ATRIBUTE_SEP + param;
            if (!isLastAttr) {
                ret = ret + DATA_SEP;
            }
        }
        return ret;
    }

    public static String setProcessInsAttribute(String name, String param,
                                                boolean isFirstAttr,
                                                boolean isLastAttr) {
        String ret = "";
        if (containsSeparator(name) || containsSeparator(param)) {
            System.err.println(separatorErrorMessage);
            new Throwable().printStackTrace();
        } else {
            if (isFirstAttr) {
                ret = PROCESS_INST_SEP;
            }

            ret = ret + name + ATRIBUTE_SEP + param;
            if (!isLastAttr) {
                ret = ret + DATA_SEP;
            }
        }
        return ret;
    }

    public static String setOwnAuditTrail(String event, String type) {
        String ret = "";
        if (containsSeparator(event) || containsSeparator(type)) {
            System.err.println(separatorErrorMessage);
            new Throwable().printStackTrace();
        } else {
            ret = ret + AUDIT_TRAIL_SEP + event + AUDIT_TRAIL_SEP + type;
        }
        return ret;
    }

    public static String setOwnProcessInstance(String prop) {
        String ret = "";
        if (containsSeparator(prop)) {
            System.err.println(separatorErrorMessage);
            new Throwable().printStackTrace();
        } else {
            int temp = 0;
            for (int i = 0; i < prop.length(); i++) {
                temp = temp + (int) prop.charAt(i);
            }
            temp = temp + 1000;

            ret = ret + PROCESS_INST_SEP + temp + PROCESS_INST_SEP + prop;
        }
        return ret;
    }

    public static String setOwnProcess(String procProp) {
        String ret = "";
        if (containsSeparator(procProp)) {
            System.err.println(separatorErrorMessage);
            new Throwable().printStackTrace();
        } else {
            int temp = 0;
            for (int i = 0; i < procProp.length(); i++) {
                temp = temp + (int) procProp.charAt(i);
            }
            temp = temp + 1000;

            ret = PROCESS_SEP + temp + PROCESS_SEP + procProp;
        }
        return ret;
    }

    public static boolean isLocked() {
        return lock;
    }

    public static void lockEnvironment() {
        lock = true;
        MXMLCommandCenter.getInstance().changeState(false);
        MXMLCommandCenter.getInstance().changeState(false);
        Logger logger = Logger.getLogger(MXMLCommandCenter.class);
        logger.setLevel(Level.FATAL);
        logger.fatal("Cannot log event, MXML log file has been deleted. Restart the application to continue logging.\n The MXMLLoggin mechanism has been severely destabilized and has been shut down");
        logger.setLevel(Level.DEBUG);
    }

    public String closeDocument() {
        BufferedReader br = null;
        BufferedWriter out = null;
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmssZ");

        try {
            String sCurrentLine;

            br = new BufferedReader(new FileReader(MXMLCommandCenter.FILE));
            sCurrentLine = br.readLine();
            if (sCurrentLine == null || sCurrentLine.equals("")) {
                System.out.println("INFO: Log file empty");
                br.close();
                return "";
            } else {
                String st = System.getProperty("user.home") + File.separator
                            + "renewlogs" + File.separator + "WorkflowLog"
                            + format.format(date) + ".mxml";
                out = new BufferedWriter(new FileWriter(st));
                out.write(sCurrentLine);
                while ((sCurrentLine = br.readLine()) != null) {
                    out.write(sCurrentLine);
                }
                out.write("</ProcessInstance></Process></WorkflowLog>");
                System.out.println("INFO: MXML log file succesfully saved as "
                                   + st);

                if (br != null) {
                    br.close();
                }
                if (out != null) {
                    out.close();
                }

                return (st);

            }
        } catch (IOException ex) {
            MXMLCommandCenter.lockEnvironment();
            return FATAL;

        }
    }

    public static String formatConstant(String name, String value, int layer) {
        String ret = "";
        if (!containsSeparator(name) && !containsSeparator(value)) {
            if (layer == AUDIT_TRAIL) {
                ret = AUDIT_TRAIL_SEP + name + AUDIT_TRAIL_SEP + value;
            } else if (layer == PROCESS_INSTANCE) {
                ret = PROCESS_INST_SEP + value + PROCESS_INST_SEP + name;
            } else if (layer == PROCESS) {
                ret = PROCESS_SEP + value + PROCESS_SEP + name;
            } else {
                System.err.println("ERROR: unspecified layer at parameter");
                new Throwable().printStackTrace();
            }
        } else {
            System.err.println(separatorErrorMessage);
            new Throwable().printStackTrace();
        }
        return ret;
    }

    private static boolean containsSeparator(String prop) {
        return (prop == null || prop.equals("")
               || prop.contains(PROCESS_INST_SEP)
               || prop.contains(AUDIT_TRAIL_SEP) || prop.contains(DATA_SEP)
               || prop.contains(ATRIBUTE_SEP) || prop.contains(PROCESS_SEP));
    }
}