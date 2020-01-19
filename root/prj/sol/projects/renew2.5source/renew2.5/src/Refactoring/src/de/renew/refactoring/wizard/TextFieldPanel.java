package de.renew.refactoring.wizard;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


/**
 * InputPanel that wraps a {@link JTextField}.
 *
 * @author 2mfriedr
 */
public abstract class TextFieldPanel extends InputPanel<String> {
    private static final long serialVersionUID = 3820675835174492959L;
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(TextFieldPanel.class);
    JTextField _textField;

    public TextFieldPanel(String intro, String defaultText) {
        super();
        setLayout(new WrapLayout());
        add(new JLabel(intro));

        _textField = new JTextField(defaultText, textFieldSize(defaultText));
        _textField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void removeUpdate(DocumentEvent e) {
                    inputChanged(getInput());
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    inputChanged(getInput());
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    inputChanged(getInput());
                }
            });
        add(_textField);
    }

    /**
     * Returns an appropriate size for the text field.
     *
     * @param text the text field's text
     * @return the size
     */
    private static int textFieldSize(final String text) {
        return Math.max(15, text.length() + 2);
    }

    @Override
    public String getInput() {
        return _textField.getText();
    }

    @Override
    public abstract void inputChanged(String input);

    @Override
    public void focus() {
        SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    _textField.requestFocusInWindow();
                }
            });
    }
}