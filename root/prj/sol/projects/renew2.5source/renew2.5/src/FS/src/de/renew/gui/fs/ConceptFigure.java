package de.renew.gui.fs;

import de.uni_hamburg.fs.JavaConcept;
import de.uni_hamburg.fs.TypeSystem;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Handle;
import CH.ifa.draw.framework.Locator;
import CH.ifa.draw.framework.ParentFigure;

import CH.ifa.draw.standard.AlignCommand;
import CH.ifa.draw.standard.ConnectionHandle;
import CH.ifa.draw.standard.RelativeLocator;

import CH.ifa.draw.util.ColorMap;
import CH.ifa.draw.util.ExtendedFont;
import CH.ifa.draw.util.Fontkit;

import de.renew.formalism.fs.ShadowConcept;

import de.renew.gui.NodeFigure;

import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;

import de.renew.util.StringUtil;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;

import java.io.IOException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ConceptFigure extends TextFigure implements NodeFigure {
    private static final String JAVADOC_REGEXP = "/\\*\\*[^\\*]*?\\*/\n";
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ConceptFigure.class);
    private static IsaConnection fgIsaPrototype = new IsaConnection();
    private static IsaConnection fgDisIsaPrototype = new IsaConnection(false);
    private static AssocConnection fgAssocPrototype = new AssocConnection();
    private String conceptStr = "";
    private String appropStr = "";
    private int typeIndex = 0; // line index of type name

    /**
     * The shadow of this place figure.
     * Initially <code>null</code>, will be created
     * when needed.
     * <p>
     * This field is transient because its information
     * can be regenerated via <code>buildShadow(...)</code>.
     * </p>
     **/
    private transient ShadowConcept shadow = null;
    private String textWithDoc;
    private List<String> slotDocumentation = new ArrayList<String>();
    private String conceptDocumentation;

    public ConceptFigure() {
        super();
        setFrameColor(ColorMap.color("Black"));
        setFillColor(ColorMap.color("White"));
    }

    public Vector<Handle> handles() {
        Vector<Handle> handles = super.handles();
        handles.addElement(new ConnectionHandle(this, RelativeLocator.north(),
                                                fgDisIsaPrototype));
        handles.addElement(new ConnectionHandle(this, RelativeLocator.south(),
                                                fgIsaPrototype));
        handles.addElement(new ConnectionHandle(this, RelativeLocator.east(),
                                                fgAssocPrototype));


        //handles.addElement(new ConnectionHandle(this, RelativeLocator.west(),
        //				    fgAssocPrototype));
        // Add association handles for all lines that contain an attribute.
        String[] lines = getLines();
        Rectangle size = displayBox();
        Rectangle[] boxes = getLineBoxes(null);
        int yOffset = 0;
        boolean skippedClassName = false;
        if (boxes != null) {
            for (int i = 0; i < lines.length; i++) {
                Locator locator = new RelativeLocator(0.0,
                                                      (2 * yOffset
                                                      + boxes[i].height) / 2.0 / size.height);
                yOffset += boxes[i].height;
                if ("".equals(lines[i])) {
                    // Ignore.
                } else if (isStereotype(lines[i])) {
                    // Ignore.
                } else if (skippedClassName) {
                    // Split at colon.
                    String name = null;
                    String type = null;
                    StringTokenizer tokenizer = new StringTokenizer(lines[i],
                                                                    ":");
                    while (type == null && tokenizer.hasMoreTokens()) {
                        if (name == null) {
                            name = tokenizer.nextToken();
                        } else {
                            type = tokenizer.nextToken();
                        }
                    }

                    if (name != null && !tokenizer.hasMoreTokens()) {
                        // Ok, all tokens are used up. Clear spaces.
                        name = StringUtil.unspace(name);
                        if (type != null) {
                            type = StringUtil.unspace(type);
                        }

                        if (name != null) {
                            if (type == null) {
                                type = "UNKNOWN";
                            }

                            // Split preceeding modifiers.
                            String frontModifier = "";
                            if (name.startsWith("#") || name.startsWith("+")
                                        || name.startsWith("-")) {
                                frontModifier = name.substring(0, 1);
                                name = StringUtil.unspace(name.substring(1));
                            }

                            String backModifiers = "";

                            // Split of modifier list enclosed in braces.
                            int bracePos = type.indexOf("{");
                            if (bracePos >= 0) {
                                backModifiers = " " + type.substring(bracePos);
                                type = StringUtil.unspace(type.substring(0,
                                                                         bracePos));
                            }

                            boolean isCollection = false;
                            while (type != null && type.endsWith("[]")) {
                                isCollection = true;
                                type = type.substring(0, type.length() - 2);
                            }

                            if (StringUtil.isNameOrEmpty(name, false)
                                        && StringUtil.isNameOrEmpty(type, true)) {
                                // This seems to be an attribute declaration, not a
                                // method call.
                                // Reattach the modifiers that were previously removed.
                                type = "." + type;
                                if (!"".equals(frontModifier)) {
                                    // How is the accesibility indicated for an association?
                                    // name=frontModifier+" "+name;
                                    // Use the field's accesibility for diplaying the class.
                                    // Is there a better solution?
                                    type = frontModifier + type;
                                }
                                name = StringUtil.unspace(name + backModifiers);


                                // Create the handle.
                                handles.addElement(new AssociationHandle(this,
                                                                         i,
                                                                         name,
                                                                         type,
                                                                         isCollection,
                                                                         locator,
                                                                         fgAssocPrototype));
                            }
                        }
                    }
                } else {
                    skippedClassName = true;
                    // Line should be class name. Skip.
                }
            }
        }
        return handles;
    }

    /* hack for setting ConceptFigures to editable: */
    public void read(CH.ifa.draw.util.StorableInput dr)
            throws IOException {
        super.read(dr);
        setReadOnly(false);


        /* hack for getting rid of parent PartitionFigure: */
        setParent(null);
    }

    private static boolean isStereotype(String str) {
        return str.startsWith("<<") || str.startsWith("\u00ab");
    }

    public String getStereotype() {
        String text = getText();
        if (isStereotype(text)) {
            int endPos = text.indexOf("\u00bb"); // look for >> as one char
            if (endPos < 0) {
                endPos = text.indexOf(">>"); // look for >> as two chars
            }
            if (endPos >= 0) {
                return text.substring(1, endPos);
            }
        }
        return "";
    }

    protected void drawLine(Graphics g, int i) {
        if (!"".equals(getLine(i))) {
            super.drawLine(g, i);
        } else {
            int y = getLineBox(g, i).y;
            int width = displayBox().width;
            g.drawLine(-5, y + 1, width - 6, y + 1);
        }
    }

    protected String getLine(int i) {
        String line = super.getLine(i);
        if (line.startsWith("\\") || line.startsWith("_")) {
            return line.substring(1);
        }
        return line;
    }

    protected Font getLineFont(int i) {
        String line = super.getLine(i);
        Font font = super.getLineFont(i); //NOTICEsignature
        int fontStyle = font.getStyle();
        if (i == typeIndex) {
            fontStyle |= Font.BOLD;
        }
        if (line.length() > 0) { // should be, otherwise getLineFont is not called.
            char styleChar = line.charAt(0);
            switch (styleChar) {
            case '\\':
                fontStyle |= Font.ITALIC;
                break;
            case '_':
                fontStyle |= ExtendedFont.UNDERLINED;
            }
        }
        return Fontkit.getFont(font.getName(), fontStyle, font.getSize());
    }

    protected int getLineAlignment(int i) {
        if (i <= typeIndex) {
            return AlignCommand.CENTERS;
        }
        return super.getLineAlignment(i); //NOTICEsignature
    }

    protected Dimension getLineDimension(int i, Graphics g) {
        if ("".equals(getLine(i))) {
            return new Dimension(0, 3);
        }
        return super.getLineDimension(i, g);
    }

    public void setText(String text) {
        if (text.equals(textWithDoc)) {
            return;
        }
        // strip javadoc comments from the shown text
        textWithDoc = text;
        parseDocumentation();

        Pattern pattern = Pattern.compile("/\\*\\*(.)*?\\*/(\\s)*\n",
                                          Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        text = matcher.replaceAll("");

        // split first and remaining lines:
        int splitPos = text.indexOf('\n');
        appropStr = "";
        typeIndex = 0;
        if (splitPos < 0) {
            conceptStr = text.trim();
            if (conceptStr.length() > 0) {
                int level = JavaConcept.getVisibilityLevel(conceptStr.charAt(0));
                if (level >= 0) {
                    conceptStr = conceptStr.substring(1);
                }
                boolean wantPackages = conceptStr.startsWith(".");
                if (wantPackages) {
                    conceptStr = conceptStr.substring(1);
                }
                TypeSystem ts = TypeSystem.instance();
                try {
                    Class<?> clazz = ts.getJavaClass(conceptStr);
                    String expandedText;
                    String[] wellKnown;
                    if (wantPackages) {
                        wellKnown = new String[0];
                    } else {
                        wellKnown = JavaConcept.getWellKnownPackages(clazz);
                    }
                    if (level >= 0) {
                        expandedText = JavaConcept.getImplementationDescription(clazz,
                                                                                level,
                                                                                wellKnown);
                    } else {
                        expandedText = ts.getJavaConcept(clazz)
                                         .getClassDescription(wellKnown);
                    }
                    setText(expandedText);
                    return;
                } catch (Throwable t) {
                }
            }
        } else {
            conceptStr = text.substring(0, splitPos).trim();
            if (isStereotype(conceptStr)) {
                // found stereotype, also use next line:
                typeIndex = 1;
                splitPos = text.indexOf('\n', splitPos + 1);
                if (splitPos < 0) {
                    conceptStr = text;
                } else {
                    conceptStr = text.substring(0, splitPos);
                }
            }
            if (splitPos >= 0) {
                while (text.length() > splitPos + 1
                               && text.charAt(splitPos + 1) == '\n') {
                    ++splitPos;
                }
                appropStr = text.substring(splitPos + 1);
            }
        }
        willChange();
        basicSetText(text);
        changed();
        logger.debug("Concept name: \"" + conceptStr + "\", approp: \""
                     + appropStr + "\"");
    }

    private void parseDocumentation() {
        conceptDocumentation = "";
        slotDocumentation = new ArrayList<String>();
        Pattern p = Pattern.compile(JAVADOC_REGEXP);
        Matcher m = p.matcher(textWithDoc);

        // count the number of definitions between the javadoc comments
        String[] partsBetweenDoc = textWithDoc.split(JAVADOC_REGEXP);
        LinkedList<Integer> defCounts = new LinkedList<Integer>();
        for (String str : partsBetweenDoc) {
            String[] lines = str.split("\n");
            int defs = 0;
            for (String line : lines) {
                // ignore empty lines and stereotypes (as a 
                // stereotype is just a part of the concept definition)
                if (line.length() > 0 && !isStereotype(line)) {
                    defs++;
                }
            }
            defCounts.add(new Integer(defs));
        }

        // if there are no definitions, we don't have anything to do
        if (defCounts.size() == 0) {
            return;
        }


        // Extract all the javadoc comments and store them in a list
        LinkedList<String> docStrings = new LinkedList<String>();
        while (m.find()) {
            String doc = textWithDoc.substring(m.start() + "/**".length(),
                                               m.end() - "*/\n".length()).trim();
            docStrings.add(doc);
        }

        // Add one to the first defCount element to 
        // make following algorithm simpler
        defCounts.set(0, defCounts.get(0) + 1);
        while (defCounts.size() > 0) {
            // defCounts[0] is one plus the number of (undocumented)
            // declarations before the first docString.
            // We first treat the concept declaration as slot declaration, too,
            // and take it later from the list
            int leadingUndocumentedSlots = defCounts.getFirst().intValue() - 1;
            while (leadingUndocumentedSlots-- > 0) {
                slotDocumentation.add(""); // no doc for this slot
            }

            // the next slot is documented by our first docString
            if (docStrings.size() > 0) {
                slotDocumentation.add(docStrings.removeFirst());
            }
            defCounts.removeFirst();
        }

        // the first "slot documentation" actually is the concept documentation
        conceptDocumentation = slotDocumentation.remove(0);
    }

    @Override
    protected void internalSetText(String newText) {
        // A bit ugly, but we can't use an initializer here,
        // as this method is called from the parent-constructor
        // when the initializer has not been run.
        if (textWithDoc == null) {
            textWithDoc = newText;
        }
        super.internalSetTextHiddenParts(textWithDoc, newText);
    }

    public Rectangle displayBox() {
        Rectangle box = super.displayBox();
        return new Rectangle(box.x - 5, box.y, box.width + 10, box.height + 1);
    }

    /** Build a shadow in the given shadow net.
      *  This shadow is stored as well as returned.
      */
    public ShadowNetElement buildShadow(ShadowNet net) {
        shadow = new ShadowConcept(net, conceptStr, appropStr,
                                   conceptDocumentation, slotDocumentation);
        shadow.context = this;
        logger.debug("shadow for concept " + conceptStr + " created!");
        return shadow;
    }

    /** Get the associated shadow, if any.
     */
    public ShadowNetElement getShadow() {
        return shadow;
    }

    public String getName() {
        return conceptStr;
    }

    public void release() {
        super.release();
        if (shadow != null) {
            shadow.discard();
        }
    }

    public boolean canBeParent(ParentFigure figure) {
        //if (figure instanceof PartitionFigure)
        //  return super.canBeParent(figure);
        // only PartitionFigures can be parents of ConceptFigures!
        //return false;
        return figure == null;
    }
}