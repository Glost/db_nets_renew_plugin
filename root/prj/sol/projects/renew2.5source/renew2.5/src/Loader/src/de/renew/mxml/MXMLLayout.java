package de.renew.mxml;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;


/**
 * This class parses log commands given to a Log4j Logger
 * to further adapt them to the MXML format.
 * <p>This class implements the abstract class {@link Layout} and its main functionality lies
 * in the {@link #format(LoggingEvent)} method, which takes a message separated by symbols,
 * segments and formats it to the MXML format by using the <code>helper</code>
 * @author Jorge Sangines
 */
public class MXMLLayout extends Layout {
    /* (non-Javadoc)
     * @see org.apache.log4j.Layout#format(org.apache.log4j.spi.LoggingEvent)
     */
    public String format(LoggingEvent event) {
        MXMLHelper helper = new MXMLHelper();
        String message = (String) event.getMessage();
        String[] secondLevel = {  };
        String attributes = "";
        String attrNumber = "";
        boolean flagProcessInst = false;
        boolean flagAudit = false;
        boolean flagProcess = false;
        int in = -1;
        String[] firstLevel = null;

        if (message.contains(MXMLCommandCenter.AUDIT_TRAIL_SEP)
                    && (!message.contains(MXMLCommandCenter.PROCESS_INST_SEP)
                               || !message.contains(MXMLCommandCenter.PROCESS_SEP))) {
            firstLevel = message.split(MXMLCommandCenter.AUDIT_TRAIL_SEP);
            flagAudit = true;
        } else if (message.contains(MXMLCommandCenter.PROCESS_INST_SEP)
                           && (!message.contains(MXMLCommandCenter.AUDIT_TRAIL_SEP)
                                      || !message.contains(MXMLCommandCenter.PROCESS_SEP))) {
            firstLevel = message.split(MXMLCommandCenter.PROCESS_INST_SEP);
            flagProcessInst = true;
        } else if (message.contains(MXMLCommandCenter.PROCESS_SEP)
                           && (!message.contains(MXMLCommandCenter.AUDIT_TRAIL_SEP)
                                      || !message.contains(MXMLCommandCenter.PROCESS_INST_SEP))) {
            firstLevel = message.split(MXMLCommandCenter.PROCESS_SEP);
            flagProcess = true;
        } else {
            firstLevel = new String[0];
        }

        if (MXMLCommandCenter.getInstance().isOn()
                    && !MXMLCommandCenter.isLocked()) {
            try {
                attrNumber = firstLevel[0];
                if (attrNumber.equals("") && firstLevel.length == 3) {
                    in = 0;
                } else {
                    in = Integer.parseInt(attrNumber);
                }
            } catch (Exception e) {
                firstLevel = new String[0];
            }

            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(MXMLCommandCenter.FILE));
            } catch (Exception e) {
                JFrame frame = new JFrame();
                JOptionPane.showMessageDialog(frame,
                                              "The MXML log file has been deleted and the mechanism shut down, restart Renew/Mulan to continue logging",
                                              "MXMLPlugin Error",
                                              JOptionPane.ERROR_MESSAGE);
                frame.dispose();

                MXMLCommandCenter.lockEnvironment();
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException exc) {
                    exc.printStackTrace();
                }
            }

            if (!MXMLCommandCenter.isLocked()) {
                if (firstLevel.length == 4) {
                    attributes = firstLevel[3];
                    secondLevel = attributes.split(MXMLCommandCenter.DATA_SEP);
                    if (secondLevel.length == in && in > 0) {
                        for (String s : secondLevel) {
                            String[] thirdLevel = s.split(MXMLCommandCenter.ATRIBUTE_SEP);
                            if (thirdLevel.length == 2) {
                                helper.addAttribute(thirdLevel[0], thirdLevel[1]);
                            } else {
                                firstLevel = new String[0];
                                break;
                            }
                        }
                    } else {
                        firstLevel = new String[0];
                    }
                }

                if (firstLevel.length > 2 && firstLevel.length < 5 && flagAudit) {
                    String ret = helper.generateAudiTrail(firstLevel[1],
                                                          firstLevel[2]);
                    System.out.println("INFO: Event |" + message
                                       + "| properly logged in mxml");
                    return ret;
                } else if (firstLevel.length > 2 && firstLevel.length < 5
                                   && flagProcessInst) {
                    String ret = helper.generateProcessInstance(firstLevel[1],
                                                                firstLevel[2]);
                    System.out.println("INFO: Event |" + message
                                       + "| properly logged in mxml");
                    return ret;
                } else if (firstLevel.length > 2 && firstLevel.length < 5
                                   && flagProcess) {
                    String ret = helper.generateProcess(firstLevel[1],
                                                        firstLevel[2]);
                    System.out.println("INFO: Event |" + message
                                       + "| properly logged in mxml");
                    return ret;
                } else {
                    System.err.println("ERROR: Event |" + message
                                       + "| was not logged in mxml due to a misconstruction "
                                       + "of the CSV parameter, check your log statement in :");
                    new Throwable().printStackTrace();
                    return "";
                }
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    /* (non-Javadoc)
     * @see org.apache.log4j.Layout#ignoresThrowable()
     */
    public boolean ignoresThrowable() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.log4j.Layout#activateOptions()
     */
    public void activateOptions() {
    }
}