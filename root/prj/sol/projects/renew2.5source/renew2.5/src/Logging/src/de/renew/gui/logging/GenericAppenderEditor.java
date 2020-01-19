/*
 * Created on 28.10.2004
 *
 */
package de.renew.gui.logging;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.lang.reflect.Method;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;


/**
 * @author Sven Offermann
 *
 */
public class GenericAppenderEditor extends JPanel {
    private static final Logger logger = Logger.getLogger(GenericAppenderEditor.class);
    private static final Set<Class<?>> supportedTypes = new HashSet<Class<?>>();

    static {
        supportedTypes.add(Integer.class);
        supportedTypes.add(Long.class);
        supportedTypes.add(Float.class);
        supportedTypes.add(Double.class);
        supportedTypes.add(Boolean.class);
        supportedTypes.add(String.class);
        supportedTypes.add(Byte.class);
        supportedTypes.add(int.class);
        supportedTypes.add(long.class);
        supportedTypes.add(float.class);
        supportedTypes.add(double.class);
        supportedTypes.add(boolean.class);
        supportedTypes.add(byte.class);
        supportedTypes.add(Priority.class);
        supportedTypes.add(Layout.class);
    }

    private Appender appender;
    private JPanel fieldsPanel;
    private JPanel buttonPanel;
    private Map<String, JComponent> attributeFields = new Hashtable<String, JComponent>();
    private Map<String, Class<?>> attributeTypes;

    public GenericAppenderEditor(Appender appender) {
        super();
        this.appender = appender;

        initComponents();
    }

    private void initComponents() {
        this.setLayout(new BorderLayout());

        fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new GridBagLayout());
        this.add(fieldsPanel, BorderLayout.CENTER);

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        this.add(buttonPanel, BorderLayout.SOUTH);

        // create fields to configure a appender
        attributeTypes = findAttributeTypes(appender);

        String[] attributes = attributeTypes.keySet().toArray(new String[] {  });
        for (int x = 0; x < attributes.length; x++) {
            Object currentValue = getCurrentValue(attributes[x], appender);

            JComponent c = createComponents(attributes[x],
                                            attributeTypes.get(attributes[x]),
                                            currentValue);
            if (c != null) {
                attributeFields.put(attributes[x], c);
            }
        }
    }

    private int row = 0;

    /**
     * creates a input field to make changes at the attributes of
     * an appender and adds the input field and a label with the
     * attribute name to the attributes panel. The input fields
     * can be Spinners to display and change number attributes,
     * TextFields for string attributes, CheckBoxes for boolean
     * attributes and ComboBoxes to set the level of an appender.
     *
     * @param aName the name of the attribute
     * @param type the type of the attribute represended by a class object
     * @param currentValue the current set value of the attribute
     * @return the created and added inputfield
     */
    private JComponent createComponents(String aName, Class<?> type,
                                        Object currentValue) {
        JComponent component = null;
        if ((type == Number.class) || (type == int.class)
                    || (type == long.class) || (type == byte.class)
                    || (type == double.class) || (type == float.class)
                    || (type == long.class)) {
            SpinnerNumberModel model = new SpinnerNumberModel();
            model.setValue(currentValue);

            component = new JSpinner(model);
            if ((type == int.class) || (type == Integer.class)) {
                model.setMinimum(new Integer(Integer.MIN_VALUE));
                model.setMaximum(new Integer(Integer.MAX_VALUE));
                model.setStepSize(new Integer(1));
            } else if ((type == byte.class) || (type == Byte.class)) {
                model.setMinimum(new Byte(Byte.MIN_VALUE));
                model.setMaximum(new Byte(Byte.MAX_VALUE));
                model.setStepSize(new Integer(1));
            } else if ((type == long.class) || (type == Long.class)) {
                model.setMinimum(new Long(Long.MIN_VALUE));
                model.setMaximum(new Long(Long.MAX_VALUE));
                model.setStepSize(new Integer(1));
            } else if ((type == float.class) || (type == Float.class)) {
                model.setMinimum(new Float(Float.MIN_VALUE));
                model.setMaximum(new Float(Float.MAX_VALUE));
            } else if ((type == double.class) || (type == Double.class)) {
                model.setMinimum(new Double(Double.MIN_VALUE));
                model.setMaximum(new Double(Double.MAX_VALUE));
            }
        } else if (type == int.class) {
            SpinnerNumberModel model = new SpinnerNumberModel();
            model.setValue(currentValue);

            component = new JSpinner(model);
        } else if (String.class.isAssignableFrom(type)) {
            component = new JTextField((String) currentValue);
        } else if (Boolean.class.isAssignableFrom(type)) {
            component = new JCheckBox();
        } else if (Priority.class.isAssignableFrom(type)) {
            Priority[] levels = { Level.ALL, Level.INFO, Level.DEBUG, Level.ERROR, Level.FATAL, Level.OFF };
            component = new JComboBox(levels);

            // set the current level
            ((JComboBox) component).getModel().setSelectedItem(currentValue);

        } else if (Layout.class.isAssignableFrom(type)) {
            /*
            Layout[] layouts = {new PatternLayout("%m%n"), new PatternLayout("%t%m%n")};
            component = new JComboBox(layouts);
            ((JComboBox) component).getModel().setSelectedItem(currentValue);*/
            if (currentValue != null) {
                component = new JTextField(((PatternLayout) currentValue)
                                .getConversionPattern());
            } else {
                component = new JTextField();
            }
        }

        if (component != null) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = row;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.anchor = GridBagConstraints.EAST;
            JLabel label = new JLabel(aName + ":");
            ((GridBagLayout) fieldsPanel.getLayout()).setConstraints(label, c);
            fieldsPanel.add(label);

            c = new GridBagConstraints();
            c.gridx = 1;
            c.gridy = row++;
            c.weightx = 0.7;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.WEST;
            c.gridwidth = 1;
            c.gridheight = 1;
            ((GridBagLayout) fieldsPanel.getLayout()).setConstraints(component,
                                                                     c);
            fieldsPanel.add(component);
        }

        return component;
    }

    private Map<String, Class<?>> findAttributeTypes(Appender appender) {
        Hashtable<String, Class<?>> setters = new Hashtable<String, Class<?>>();
        Class<?> clazz = appender.getClass();
        Method[] methods = clazz.getMethods();
        for (int x = 0; x < methods.length; x++) {
            if (methods[x].getName().startsWith("set")) {
                if (methods[x].getParameterTypes().length == 1) {
                    if (supportedTypes.contains(methods[x].getParameterTypes()[0])) {
                        String name = methods[x].getName()
                                                .substring("set".length());
                        setters.put(name, methods[x].getParameterTypes()[0]);
                    }
                }
            }
        }

        TreeMap<String, Class<?>> attributeTypes = new TreeMap<String, Class<?>>();
        for (int x = 0; x < methods.length; x++) {
            if (methods[x].getName().startsWith("get")) {
                String name = methods[x].getName().substring("get".length());
                if (setters.containsKey(name)) {
                    if (methods[x].getParameterTypes().length == 0) {
                        if (supportedTypes.contains(methods[x].getReturnType())) {
                            attributeTypes.put(name, methods[x].getReturnType());
                        }
                    }
                }
            }
        }

        return attributeTypes;
    }

    /**
     * gets the current value of an appender attribute
     *
     * @param aName the name of the attribute
     * @param appender the appender
     * @return the current value of the attribute of the given appender
     */
    private Object getCurrentValue(String aName, Appender appender) {
        Object value = null;
        Class<?> clazz = appender.getClass();
        try {
            Method method = clazz.getMethod("get" + aName, new Class<?>[] {  });

            value = method.invoke(appender, new Object[] {  });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return value;
    }

    /**
     * sets a given attribute of an given appender to the new
     * given value.
     *
     * @param appender the appender of which the attribute should be changed.
     * @param aName the name of the attribute which should be set
     * @param value the value to which the attribute should be set
     *         */
    private void setValue(Appender appender, String aName, Object value) {
        Class<?> clazz = appender.getClass();
        if (value != null) {
            Class<?> type = value.getClass();
            if (type == Level.class) {
                type = Priority.class;
            } else if (type == PatternLayout.class) {
                type = Layout.class;
            }

            Method method = null;
            try {
                method = clazz.getMethod("set" + aName, new Class<?>[] { type });

            } catch (NoSuchMethodException ex) {
                try {
                    if (type == Integer.class) {
                        method = clazz.getMethod("set" + aName,
                                                 new Class<?>[] { int.class });
                    } else if (type == Byte.class) {
                        method = clazz.getMethod("set" + aName,
                                                 new Class<?>[] { byte.class });
                    } else if (type == Long.class) {
                        method = clazz.getMethod("set" + aName,
                                                 new Class<?>[] { long.class });
                    } else if (type == Float.class) {
                        method = clazz.getMethod("set" + aName,
                                                 new Class<?>[] { float.class });
                    } else if (type == Double.class) {
                        method = clazz.getMethod("set" + aName,
                                                 new Class<?>[] { double.class });
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            try {
                if (method != null) {
                    value = method.invoke(appender, new Object[] { value });

                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * applies the made changes to the attributes of the appender.
     * This method is called when the user pressed the apply button.
     */
    public void applyChanges() {
        String[] attributes = attributeFields.keySet().toArray(new String[] {  });

        for (int x = 0; x < attributes.length; x++) {
            Class<?> type = attributeTypes.get(attributes[x]);
            JComponent c = attributeFields.get(attributes[x]);

            if ((type != null) && (c != null)) {
                if (c instanceof JTextField) {
                    String text = ((JTextField) c).getText();

                    if (attributes[x].equals("Layout")) {
                        setValue(appender, attributes[x],
                                 new PatternLayout(text));
                    } else {
                        setValue(appender, attributes[x], text);
                    }
                } else if (c instanceof JSpinner) {
                    SpinnerModel model = ((JSpinner) c).getModel();
                    setValue(appender, attributes[x], model.getValue());
                } else if (c instanceof JCheckBox) {
                    setValue(appender, attributes[x],
                             new Boolean(((JCheckBox) c).isSelected()));
                } else if (c instanceof JComboBox) {
                    setValue(appender, attributes[x],
                             ((JComboBox) c).getModel().getSelectedItem());
                }
            }
        }
    }
}