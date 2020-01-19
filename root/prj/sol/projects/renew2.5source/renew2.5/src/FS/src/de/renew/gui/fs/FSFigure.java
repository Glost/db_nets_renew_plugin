package de.renew.gui.fs;

import collections.CollectionEnumeration;
import collections.HashedMap;
import collections.HashedSet;
import collections.LinkedList;
import collections.UpdatableMap;
import collections.UpdatableSeq;
import collections.UpdatableSet;

import de.uni_hamburg.fs.BasicType;
import de.uni_hamburg.fs.EquivRelation;
import de.uni_hamburg.fs.FSNode;
import de.uni_hamburg.fs.FeatureStructure;
import de.uni_hamburg.fs.JavaObject;
import de.uni_hamburg.fs.ListType;
import de.uni_hamburg.fs.Name;
import de.uni_hamburg.fs.Node;
import de.uni_hamburg.fs.NullObject;
import de.uni_hamburg.fs.Path;
import de.uni_hamburg.fs.TagMap;
import de.uni_hamburg.fs.Type;

import CH.ifa.draw.framework.Handle;

import CH.ifa.draw.standard.RelativeLocator;

import CH.ifa.draw.util.ColorMap;
import CH.ifa.draw.util.Fontkit;
import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import de.renew.formalism.fs.FSNetCompiler;
import de.renew.formalism.fs.FSNetParser;
import de.renew.formalism.fs.FSNetPreprocessor;
import de.renew.formalism.fs.SingleFSNetCompiler;

import de.renew.gui.CPNTextFigure;
import de.renew.gui.NetInstanceHandle;
import de.renew.gui.SemanticUpdateFigure;

import de.renew.net.NetInstance;

import de.renew.remote.NetInstanceAccessor;
import de.renew.remote.RemotePlugin;

import de.renew.shadow.ShadowNet;
import de.renew.shadow.SyntaxException;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import java.io.IOException;
import java.io.ObjectInputStream;

import java.util.Enumeration;
import java.util.Vector;


public class FSFigure extends CPNTextFigure implements SemanticUpdateFigure {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(FSFigure.class);
    public static final int SHUTTERSIZE = 7;
    public static final String ELLIPSE = "...";
    transient private FSPlugin fsPlugin = null;
    transient private boolean alwaysUML = false;
    transient private boolean automatic = false;
    transient private FeatureStructure fs;
    transient private TagMap tagmap;
    transient private FontMetrics metrics;
    transient private FontMetrics boldMetrics;
    private transient int lineh;
    private transient int d;
    private transient int ascent;
    private transient Dimension fExtent = null;
    private transient UpdatableSeq subfigs = null; //of TextSubFigures
    private transient UpdatableSeq boldfigs = null; //of TextSubFigures
    private transient UpdatableSet openNodes = new HashedSet();

    // of Nodes which are openNodes
    private transient Node selectedNode = null;
    private transient UpdatableSeq handles = null; // of Tag- and ShutterHandles

    /**
     * If we only update handle rects, this variable contains the
     * current index within handles, -1 otherwise.
     **/
    private transient int updateHandleIndex = -1;

    public FSFigure(Object token) {
        this(new FeatureStructure(JavaObject.getJavaType(token)), false);
        alwaysUML = true;
    }

    public FSFigure(FeatureStructure fs) {
        this(fs, false);
    }

    public FSFigure(FeatureStructure fs, boolean expanded) {
        super(INSCRIPTION);
        setFrameColor(Color.black); // so that TextFigure treats us as a box
        this.fs = fs;
        automatic = true;
        tagmap = new TagMap(fs.getRoot());
        setReadOnly(true);
        super.setText(" "); // non-empty, so that this figure is not removed!
        setAlignment(LEFT);
        //super.setText(fs.toString());
        if (expanded) {
            // initially all nodes are open:
            openNodes.includeElements(fs.getNodes());


            // for JavaObjects, open first level:
            openNodes.include(fs.getRoot());
        }
    }

    public FSFigure() {
        super(INSCRIPTION);
        setFrameColor(Color.black); // so that TextFigure treats us as a box
        setText("[]");
        setReadOnly(false);
    }

    protected void basicSetText(String newText) {
        super.basicSetText(newText);
        if (!automatic) {
            try {
                semanticUpdate(null);
            } catch (SyntaxException se) {
                fs = null;
                logger.error("Syntax Exception in Feature Structure:\n" + se);
            }
        }
    }

    FeatureStructure getFeatureStructure() {
        return fs;
    }

    public void semanticUpdate(ShadowNet shadowNet) throws SyntaxException {
        if (automatic) {
            return;
        }
        synchronized (this) {
            updateHandleIndex = -1;
            SingleFSNetCompiler compiler = new SingleFSNetCompiler();
            FSNetParser fsParser = new FSNetParser(new java.io.StringReader(getText()));
            if (shadowNet != null) {
                fsParser.setDeclarationNode(compiler.makeDeclarationNode(shadowNet));
            } else {
                fsParser.setDeclarationNode(null);
            }
            UpdatableMap tags = new HashedMap();
            EquivRelation er = new EquivRelation();
            selectedNode = null;
            Node root = null;
            try {
                root = fsParser.parseFS(tags, er, Path.EPSILON,
                                        new Vector<Path>(), new Vector<Object>());
            } catch (de.renew.formalism.java.ParseException ex) {
                SyntaxException e = FSNetCompiler.makeSyntaxException(ex);
                e.addObject(this);
                changed();
                throw e;
            }
            try {
                er.extensionalize();
                root = er.rebuild(root);
            } catch (Exception uff) {
                SyntaxException e = new SyntaxException("FS not extensionalizable!",
                                                        uff);
                e.addObject(this);
                changed();
                throw e;
            }
            fs = new FeatureStructure(root, false);
            tagmap = new TagMap(root, er, tags);


            // logger.debug("Setting all FSNodes to open!");
            openNodes = new HashedSet();


            // initially all fs-nodes are open:
            openNodes.includeElements(fs.getNodes());


            // for JavaObjects, open first level:
            openNodes.include(root);
            changed();
            // logger.debug("FS: "+new FeatureStructure(fs));
        }
    }

    public Vector<Handle> handles() {
        Vector<Handle> allHandles = super.handles();
        if (!automatic && fs != null && fs.getFirstMissingAssociation() != null) {
            allHandles.addElement(new FeatureConnectionHandle(this,
                                                              RelativeLocator
                .east()));
        }
        synchronized (this) {
            if (handles != null) {
                CollectionEnumeration handleEnum = handles.elements();
                while (handleEnum.hasMoreElements()) {
                    allHandles.addElement((Handle) handleEnum.nextElement());
                }
            }
        }
        return allHandles;
    }

    public static Font getBoldFont(Font font) {
        return Fontkit.getFont(font.getName(), font.getStyle() | Font.BOLD,
                               font.getSize());
    }

    /**
     * Initialises private variables in relationship with FontMetrics
     * to default values depending on the font of this FSFigure.
     */
    private void initMetrics() {
        metrics = getDefaultFontMetrics(getFont());
        boldMetrics = getDefaultFontMetrics(getBoldFont(getFont()));
        lineh = metrics.getHeight();
        d = metrics.stringWidth(" ");
        ascent = metrics.getAscent() - 1;
    }

    private boolean renderAsUml() {
        if (alwaysUML) {
            return true;
        }
        if (fsPlugin == null) {
            fsPlugin = FSPlugin.getCurrent();
        }
        if (fsPlugin != null) {
            return fsPlugin.getUmlRenderMode();
        }
        return false;
    }

    public void setup() {
        boolean umlMode = renderAsUml();
        if (fExtent == null) {
            synchronized (this) {
                tagmap.resetVisited();
                initMetrics();
                subfigs = new LinkedList();
                boldfigs = new LinkedList();
                if (updateHandleIndex < 0) {
                    handles = new LinkedList();
                    // logger.debug("New handle list.");
                }
                Node root = fs.getRoot();
                String tagstr = tagmap.getTag(root).toString();

                // handle very special case: FS is one tag only:
                if (!umlMode && root.getType().equals(Type.TOP)
                            && tagstr.length() > 0) {
                    if (!handles.isEmpty()) {
                        handles = new LinkedList();
                    }
                    int tagwidth = metrics.stringWidth(tagstr) + d;
                    subfigs.insertFirst(new TextSubFigure(tagstr, (d + 1) / 2,
                                                          ascent));
                    subfigs.insertFirst(new FilledRectSubFigure(0, 0, tagwidth,
                                                                lineh - 2));
                    fExtent = new Dimension(tagwidth, lineh - 2);
                    updateHandleIndex = -1;
                } else {
                    fExtent = setupFS(0, 0, root, Type.TOP, umlMode);
                    updateHandleIndex = 0;
                }

                //logger.debug("Setting update handle index to 0.");
            }
        }
    }

    public Rectangle displayBox() {
        Rectangle box = super.displayBox();
        if (fs == null) {
            return box;
        }
        setup();
        return new Rectangle(box.x, box.y, fExtent.width, fExtent.height);
    }

    public void internalDraw(Graphics g) {
        if (fs == null) {
            super.internalDraw(g);
        } else {
            synchronized (this) {
                setup();
                Rectangle box = super.displayBox();
                Color fill = getFillColor();
                if (!ColorMap.isTransparent(fill)) {
                    g.setColor(fill);
                    g.fillRect(box.x, box.y, fExtent.width, fExtent.height);
                }
                g.setColor((Color) getAttribute("TextColor"));
                g.setFont(getFont());
                g.translate(box.x, box.y);
                CollectionEnumeration subfigenumeration = subfigs.elements();
                while (subfigenumeration.hasMoreElements()) {
                    ((Drawable) subfigenumeration.nextElement()).draw(g);
                }
                g.setFont(getBoldFont(getFont()));
                subfigenumeration = boldfigs.elements();
                while (subfigenumeration.hasMoreElements()) {
                    ((Drawable) subfigenumeration.nextElement()).draw(g);
                }
                g.translate(-box.x, -box.y);
            }
        }
    }

    private boolean addShutter(Node fs, int x, int y) {
        boolean isClosed = !openNodes.includes(fs);
        Rectangle shutterBox = new Rectangle(x - SHUTTERSIZE / 2,
                                             y - SHUTTERSIZE / 2, SHUTTERSIZE,
                                             SHUTTERSIZE);
        if (updateHandleIndex < 0) {
            handles.insertLast(new ShutterHandle(this, shutterBox, fs, isClosed));
        } else {
            ShutterHandle sh = (ShutterHandle) handles.at(updateHandleIndex++);
            if (sh.node != fs) {
                logger.error("test failed: ShutterHandle references " + sh.node
                             + " instead of " + fs);
            }
            sh.setBox(shutterBox);
            sh.isClosed = isClosed;
        }
        return isClosed;
    }

    private Dimension setupList(int x, int y, int maxh, Node fs, boolean umlMode) {
        int w = 0;
        Type listtype = fs.getType();
        try {
            ListType list = (ListType) listtype;
            Type elemtype = list.getBaseType();

            if (list.getSubtype() == ListType.NELIST) {
                if (openNodes.includes(fs)) {
                    // list node is not closed:
                    Node head;
                    if (fs.hasFeature(ListType.HEAD)) {
                        head = fs.delta(ListType.HEAD);
                    } else {
                        head = new FSNode(elemtype);
                    }
                    Dimension headdim = setupFS(x, y, head, elemtype, umlMode);
                    maxh = Math.max(maxh, headdim.height);
                    w = headdim.width;

                    Node tail;
                    if (fs.hasFeature(ListType.TAIL)) {
                        tail = fs.delta(ListType.TAIL);
                    } else {
                        tail = new FSNode(ListType.getList(elemtype));
                    }
                    Dimension taildim = null;
                    Name tag = tagmap.getTag(tail);
                    listtype = tail.getType();
                    if (listtype instanceof ListType) {
                        ListType tailtype = (ListType) listtype;
                        if (tailtype.getSubtype() == ListType.NELIST
                                    && tag.equals(de.uni_hamburg.fs.Name.EMPTY)
                                    && tailtype.getBaseType().equals(elemtype)
                                    || tailtype.getSubtype() == ListType.ELIST) {
                            taildim = setupList(x + w + 2 * d, y, maxh, tail,
                                                umlMode);
                            if (taildim.width > 0) {
                                w += 2 * d;
                            }
                        }
                        if (taildim == null) {
                            taildim = setupFS(x + w + 3 * d - 1, y, tail,
                                              ListType.getList(elemtype),
                                              umlMode);


                            // add list seperator line:
                            subfigs.insertFirst(new LineSubFigure(x + w
                                                                  + 3 * d / 2,
                                                                  y, 0, maxh
                                                                  - 1));
                            w += 3 * d; // space for vertical line
                        }
                        w += taildim.width;
                        maxh = Math.max(maxh, taildim.height);
                    }
                }
            }
        } catch (ClassCastException cce) {
            logger.error("!!!corrupted list! Unexpected type: " + listtype);
        }
        return new Dimension(w, maxh);
    }

    private Dimension setupFS(int x, int y, Node fs, Type type, boolean umlMode) {
        if (fs == null) {
            return new Dimension(0, 0);
        }
        int maxh = lineh;
        int maxw = 0;
        Type nodetype = fs.getType();
        int left = x;
        int upper = y;
        boolean isNode = !(nodetype instanceof BasicType
                         && ((BasicType) nodetype).isObject()
                         || nodetype instanceof NullObject);
        boolean isList = false;
        if (nodetype instanceof ListType) {
            int subtype = ((ListType) nodetype).getSubtype();
            if ((subtype == ListType.ELIST || subtype == ListType.NELIST)
                        && !tagmap.isVisited(fs)) {
                isList = true;
            }
        }
        if (isNode) {
            left += d; // space for square bracket
            if (isList) {
                left += d; // additional space for angles
                maxh += 2; // increase default (empty) list height

                // convert types to base types:
                if (type instanceof ListType) {
                    type = ((ListType) type).getBaseType();
                }
                nodetype = ((ListType) nodetype).getBaseType();
            } else {
                upper += 2;
            }
        }
        boolean noFirstLine = true;
        Rectangle tagbox = null;
        Name tag = tagmap.getTag(fs);
        if (!tag.equals(de.uni_hamburg.fs.Name.EMPTY)) {
            // tags are only drawn for taged nodes, not for basic types or
            // nodes without features of an extensional type.
            String tagstr = tag.name;
            if (nodetype instanceof JavaObject
                        && ((JavaObject) nodetype).getJavaObject() instanceof NetInstance) {
                tagstr = ((JavaObject) nodetype).getJavaObject().toString();
            }
            int tagwidth;
            if (umlMode) {
                tagwidth = boldMetrics.stringWidth(tagstr);
                if (nodetype.isInstanceType()) {
                    maxh += 1; // for underline
                    Drawable tagUL = new LineSubFigure(left,
                                                       upper + ascent + 1,
                                                       tagwidth, 0);
                    subfigs.insertFirst(tagUL);
                }
                boldfigs.insertFirst(new TextSubFigure(tagstr, left,
                                                       upper + ascent));
            } else {
                tagwidth = metrics.stringWidth(tagstr) + d;
                subfigs.insertFirst(new TextSubFigure(tagstr,
                                                      left + (d + 1) / 2,
                                                      upper + ascent));
                Drawable tagBX = new FilledRectSubFigure(left, upper, tagwidth,
                                                         lineh - 2);
                subfigs.insertFirst(tagBX);
            }
            tagbox = new Rectangle(left, upper, tagwidth, lineh - 2);
            maxw = tagwidth;
            noFirstLine = false;
        }
        if (!isNode || !tagmap.visit(fs)) {
            if (!isList && nodetype.getName().equals(FSNetPreprocessor.LINK)) {
                // special rendering of FS-Up-/Downlinks:
                int lx = 0;
                if (fs.hasFeature(FSNetPreprocessor.RCV)) {
                    Dimension rcvdim = setupFS(x, y,
                                               fs.delta(FSNetPreprocessor.RCV),
                                               nodetype.appropType(FSNetPreprocessor.RCV),
                                               umlMode);
                    maxh = rcvdim.height;
                    lx = rcvdim.width + d;
                }
                Dimension subdim = setupFS(x + lx + 2 * d, y,
                                           fs.delta(FSNetPreprocessor.PARAM),
                                           nodetype.appropType(FSNetPreprocessor.PARAM),
                                           umlMode);
                maxh = Math.max(maxh, subdim.height);
                maxw = lx + subdim.width + 2 * d;
                subfigs.insertFirst(new TextSubFigure(":", x + lx, y + ascent));
                return new Dimension(maxw, maxh);
            } else {
                // draw optional first line with type (after tag)
                String typestr = "";
                if (!nodetype.equals(type)) {
                    typestr = nodetype.getName();
                }
                if (typestr.length() > 0) {
                    // logger.debug("nodetype: "+nodetype+", default type: "+type);
                    if (umlMode && isNode) {
                        typestr = ":" + typestr;
                    }
                    if (!umlMode && !tag.equals(de.uni_hamburg.fs.Name.EMPTY)) {
                        maxw += d; // space between tag and type
                    }
                    Rectangle typeRect = new Rectangle(left + maxw, upper,
                                                       boldMetrics.stringWidth(typestr),
                                                       lineh);
                    boldfigs.insertFirst(new TextSubFigure(typestr, typeRect.x,
                                                           upper + ascent));
                    if (isNode && nodetype.isInstanceType()) {
                        subfigs.insertFirst(new LineSubFigure(left + maxw,
                                                              upper + ascent
                                                              + 1,
                                                              typeRect.width, 0));
                        if (!umlMode) {
                            maxh += 1; // space for underline
                        }
                    }
                    maxw += typeRect.width;
                    noFirstLine = false;
                }
                CollectionEnumeration features = fs.featureNames();
                boolean hasFeatures = features.hasMoreElements();
                if (hasFeatures) {
                    if (!openNodes.includes(fs)) {
                        subfigs.insertFirst(new TextSubFigure(ELLIPSE,
                                                              left + maxw,
                                                              upper + ascent));
                        maxw += metrics.stringWidth(ELLIPSE);
                    } else {
                        if (noFirstLine) {
                            maxh = 0;
                        } else if (umlMode && !noFirstLine) {
                            maxh += 1; // space for separating line
                        }
                        if (isList) {
                            int listdx = 1;
                            int listdy = maxh;
                            if (typestr.length() == 0 && maxw > 0) {
                                // place list right of Tag:
                                listdx = maxw + d;
                                listdy = 0;
                            }
                            Dimension listdim = setupList(left + listdx,
                                                          upper + listdy, 0,
                                                          fs, umlMode);
                            maxw = Math.max(maxw, listdx + listdim.width);
                            maxh = Math.max(maxh, listdy + listdim.height);
                        } else {
                            do {
                                Name featureName = (Name) features.nextElement();
                                String feature = featureName.toString()
                                                 + (umlMode ? "=" : ": ");
                                int indent = metrics.stringWidth(feature);
                                Dimension subdim = setupFS(left + indent,
                                                           upper + maxh,
                                                           fs.delta(featureName),
                                                           nodetype.appropType(featureName),
                                                           umlMode);
                                subfigs.insertFirst(new TextSubFigure(feature,
                                                                      left,
                                                                      upper
                                                                      + maxh
                                                                      + (subdim.height
                                                                        - lineh) / 2
                                                                      + ascent));
                                maxw = Math.max(maxw, indent + subdim.width);
                                maxh += subdim.height + 1; // + d;
                            } while (features.hasMoreElements());
                            maxh += 1;
                            if (umlMode && !noFirstLine) {
                                subfigs.insertFirst(new LineSubFigure(x,
                                                                      y + lineh
                                                                      + 1,
                                                                      maxw
                                                                      + 2 * d
                                                                      - 1, 0));
                            }
                        }
                    }
                }
                if (hasFeatures) {
                    if (isList) {
                        addShutter(fs, x, y + maxh / 2);
                    } else {
                        addShutter(fs, x, y + lineh / 2);
                    }
                }
            }
        }
        if (maxw == 0) {
            maxw = d;
        }
        if (isNode) {
            maxw += 2 * d;
            if (isList) {
                maxw += 2 * d;
            } else {
                maxh += 2;
            }
            Drawable tagsubfigure;
            if (umlMode && !isList) {
                subfigs.insertFirst(new FilledRectSubFigure(x, y, maxw, maxh));
                tagsubfigure = new RectSubFigure(x, y, maxw, maxh);
            } else {
                tagsubfigure = new BracketSubFigure(x, y, maxw, maxh,
                                                    isList ? 2 * d - 1 : d - 1,
                                                    isList);
                subfigs.insertFirst(tagsubfigure);
            }
            if (tagbox != null) {
                boolean successfulNetInstanceHandle = false;
                if (nodetype instanceof JavaObject
                            && ((JavaObject) nodetype).getJavaObject() instanceof NetInstance) {
                    {
                        if (updateHandleIndex < 0) {
                            // TODO Check whether the remote layer should
                            // be used in a more general way than just
                            // wrapping a local object.
                            NetInstanceAccessor niacc = RemotePlugin.getInstance()
                                                                    .wrapInstance((NetInstance) ((JavaObject) nodetype)
                                                                                  .getJavaObject());
                            handles.insertLast(new NetInstanceHandle(this,
                                                                     tagbox,
                                                                     niacc));
                        } else {
                            NetInstanceHandle nih = (NetInstanceHandle) handles
                                                    .at(updateHandleIndex++);
                            nih.setBox(tagbox);
                        }
                        successfulNetInstanceHandle = true;
                    }
                }

                if (!successfulNetInstanceHandle) {
                    if (updateHandleIndex < 0) {
                        // create new tag handles:
                        handles.insertLast(new TagHandle(this, tagbox,
                                                         tagsubfigure, fs,
                                                         fs.equals(selectedNode)));
                    } else {
                        // update existing tag handles:
                        // logger.debug("Updating handle "+updateHandleIndex);
                        TagHandle th = (TagHandle) handles.at(updateHandleIndex++);
                        if (th.node != fs) {
                            logger.error("test failed: TagHandle references "
                                         + th.node + " instead of " + fs);
                        }
                        th.setBox(tagbox);
                        th.setHighlight(tagsubfigure);
                        th.selected = fs.equals(selectedNode);
                    }
                }
            }
        }
        return new Dimension(maxw, maxh);
    }

    public void read(StorableInput dr) throws IOException {
        if (dr.getVersion() <= 5) {
            // Hack for old FS-Figures:
            super.readWithoutType(dr);
        } else {
            super.read(dr);
            setFrameColor(Color.black); // so that TextFigure treats us as a box
            if (dr.getVersion() > 6) {
                // read list of paths for open nodes:
                openNodes = new HashedSet();
                int noPaths = dr.readInt();
                Node root = null;
                if (fs != null) {
                    root = fs.getRoot();
                }
                while (noPaths-- > 0) {
                    String pathStr = dr.readString();
                    if (root != null) {
                        Path path = new Path(pathStr);
                        openNodes.include(root.delta(path));
                    }
                }
            }
        }
    }

    public void write(StorableOutput dw) {
        super.write(dw);
        if (fs == null || openNodes == null) {
            dw.writeInt(0);
        } else {
            dw.writeInt(openNodes.size());
            CollectionEnumeration nodes = openNodes.elements();
            while (nodes.hasMoreElements()) {
                dw.writeString(fs.onePathTo((Node) nodes.nextElement())
                                 .toString());
            }
        }
    }

    protected void markDirty() {
        fExtent = null;
        super.markDirty();
    }

    private Enumeration<Node> getListNodes(Node node) {
        Vector<Node> listNodes = new Vector<Node>();
        listNodes.addElement(node);
        Type listType = node.getType();
        if (listType instanceof ListType
                    && ((ListType) listType).getSubtype() == ListType.NELIST) {
            while (true) {
                node = node.delta(ListType.TAIL);
                if (!node.getType().equals(listType)
                            || listNodes.contains(node)) {
                    break;
                }
                listNodes.addElement(node);
            }
        }
        return listNodes.elements();
    }

    void setNodeShutState(Node node, boolean close, boolean deep) {
        synchronized (this) {
            Enumeration<Node> nodes;
            if (deep) {
                nodes = new FeatureStructure(node, false).getNodes();
            } else {
                nodes = getListNodes(node);
            }
            if (shutOrClose(nodes, close)) {
                handlesChanged();
                invalidate();
                markDirty();
                selectedNode = null;
                updateHandleIndex = -1;
                changed();
            }
        }
    }

    private boolean shutOrClose(Enumeration<Node> nodes, boolean close) {
        boolean changed = false;
        while (nodes.hasMoreElements()) {
            changed |= shutOrClose(nodes.nextElement(), close);
        }
        return changed;
    }

    private boolean shutOrClose(Node node, boolean shut) {
        boolean isClosed = !openNodes.includes(node);
        if (isClosed == shut) {
            return false;
        }
        if (shut) {
            openNodes.removeOneOf(node);
        } else {
            openNodes.include(node);
        }
        return true;
    }

    void setSelectedTag(Node node) {
        synchronized (this) {
            if (node.equals(selectedNode)) {
                // already selected, toggle to unselected
                selectedNode = null;
            } else {
                selectedNode = node;
            }
            invalidate();
            CollectionEnumeration tags = handles.elements();
            while (tags.hasMoreElements()) {
                Object handle = tags.nextElement();
                if (handle instanceof TagHandle) {
                    TagHandle th = (TagHandle) handle;
                    th.selected = th.node.equals(selectedNode);
                }
            }
            if (selectedNode != null) {
                // make all tagrects of this tag visible:
                if (shutOrClose(fs.backwardsReachableNodes(selectedNode), false)) {
                    handlesChanged();
                    markDirty();
                    updateHandleIndex = -1;
                }
            }
            changed();
        }
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        basicSetText(getText());
    }
}

abstract class PosSubFigure implements Drawable {
    int x;
    int y;

    PosSubFigure(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

class TextSubFigure extends PosSubFigure {
    String text;

    TextSubFigure(String text, int x, int y) {
        super(x, y);
        this.text = text;
    }

    public void draw(Graphics g) {
        g.drawString(text, x, y);
    }
}

class RectSubFigure extends PosSubFigure {
    int width;
    int height;

    RectSubFigure(int x, int y, int width, int height) {
        super(x, y);
        this.width = width;
        this.height = height;
    }

    public void draw(Graphics g) {
        g.drawRect(x, y, width - 1, height - 1);
    }
}

class FilledRectSubFigure extends RectSubFigure {
    FilledRectSubFigure(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public void draw(Graphics g) {
        Color penCol = g.getColor();
        g.setColor(Color.white);
        g.fillRect(x, y, width, height);
        g.setColor(penCol);
        super.draw(g);
    }
}

class LineSubFigure extends PosSubFigure {
    int width;
    int height;

    LineSubFigure(int x, int y, int width, int height) {
        super(x, y);
        this.width = width;
        this.height = height;
    }

    public void draw(Graphics g) {
        g.drawLine(x, y, x + width, y + height);
    }
}

class BracketSubFigure implements Drawable {
    int[] leftBracketX;
    int[] leftBracketY;
    int[] rightBracketX;
    int[] rightBracketY;

    BracketSubFigure(int x, int y, int width, int height, int d, boolean angle) {
        int xr = x + width - 1;
        int yu = y + height - 1;
        if (angle) {
            int ym = y + height / 2;
            leftBracketX = new int[] { x + d, x, x + d };
            leftBracketY = new int[] { y, ym, yu };
            rightBracketX = new int[] { xr - d, xr, xr - d };
            rightBracketY = new int[] { y, ym, yu };
        } else {
            leftBracketX = new int[] { x + d, x, x, x + d };
            leftBracketY = new int[] { y, y, yu, yu };
            rightBracketX = new int[] { xr - d, xr, xr, xr - d };
            rightBracketY = new int[] { y, y, yu, yu };
        }
    }

    public void draw(Graphics g) {
        g.drawPolyline(leftBracketX, leftBracketY, leftBracketX.length);
        g.drawPolyline(rightBracketX, rightBracketY, rightBracketX.length);
    }
}