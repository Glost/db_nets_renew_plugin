package de.uni_hamburg.fs;

import collections.CollectionEnumeration;


public class PrettyPrinter {
    private TagMap map;

    private PrettyPrinter(Node node) {
        map = new TagMap(node);
    }

    public static void println(FeatureStructure fs) {
        System.out.println(toString(fs));
    }

    public static String toString(FeatureStructure fs) {
        Node node = fs.getRoot();
        return "\n" + new PrettyPrinter(node).toString(node, "\n ", Type.TOP)
               + "\n";
    }

    private String toString(Node thiz, String indent, Type defaultType) {
        StringBuffer output = new StringBuffer();

        //output.append('*').append(seqNo); indent += FeatureStructure.indent(output.length());
        Type nodetype = thiz.getType();
        boolean isNode = !(nodetype instanceof BasicType
                         && ((BasicType) nodetype).isObject()
                         || nodetype instanceof NullObject);
        if (isNode) {
            Name tag = map.getTag(thiz);
            if (!tag.equals(Name.EMPTY)) {
                output = output.append("#").append(tag.name);
                indent += indent(output.length());
            }
            if (!map.visit(thiz)) {
                if (thiz instanceof JavaObject) {
                    output.append(thiz.toString());
                } else {
                    output.append(untaggedToString(thiz, indent, defaultType));
                }
            }
        } else {
            output.append(untaggedToString(thiz, indent, defaultType));
        }
        return output.toString();
    }

    private String listToString(Node thiz, String indent, Type defaultType) {
        StringBuffer output = new StringBuffer();
        try { // catch ClassCast between Type and ListType!
            ListType list = (ListType) thiz.getType();
            Type elemtype = list.getBaseType();

            if (list.getSubtype() == ListType.LIST) {
                output.append("| ")
                      .append(toString(thiz, indent + "   ", defaultType));
            } else {
                Type defaultElemType = null;
                if (defaultType instanceof ListType) {
                    defaultElemType = ((ListType) defaultType).getBaseType();
                } else {
                    // default type is not a list type; does not matter.
                    defaultElemType = Type.TOP;
                }
                if (!elemtype.equals(defaultElemType)) {
                    String typename = elemtype.getName();
                    output.append(typename);
                    indent += indent(typename.length());
                }
                if (list.getSubtype() == ListType.NELIST) {
                    //if (thiz.hasFeature(ListType.HEAD))
                    output = output.append(toString(thiz.delta(ListType.HEAD),
                                                    indent + " ", elemtype))
                                   .append(indent);


                    //else
                    //output = output.append("(no head feature!)");
                    //if (thiz.hasFeature(ListType.TAIL)) {
                    Node tailfs = thiz.delta(ListType.TAIL);
                    Name tag = map.getTag(tailfs);
                    Type listtype = tailfs.getType();
                    boolean isTailList = false;
                    if (listtype instanceof ListType) {
                        ListType tailtype = (ListType) tailfs.getType();
                        if (tag.equals(Name.EMPTY)
                                    && tailtype.getBaseType().equals(elemtype)
                                    || tailtype.getSubtype() == ListType.ELIST) {
                            output = output.append(listToString(tailfs, indent,
                                                                ListType.getList(elemtype)));
                            isTailList = true;
                        }
                    }
                    if (!isTailList) {
                        output = output.append("| ")
                                       .append(toString(tailfs, indent + "   ",
                                                        ListType.getList(elemtype)));
                    }


                    //} else
                    //  output = output.append("(no tail feature!)");
                }
            }
        } catch (ClassCastException ex) {
            output.append("!!!corrupted list!!!");
        }
        return output.toString();
    }

    private String innerToString(Node thiz, String indent, Type defaultType) {
        StringBuffer output = new StringBuffer();
        String whitespace = "";
        Type nodetype = thiz.getType();
        if (!nodetype.equals(defaultType)) {
            output = output.append(nodetype.getName());
            whitespace = indent;
        }
        CollectionEnumeration features = thiz.featureNames();
        while (features.hasMoreElements()) {
            Name featureName = (Name) features.nextElement();
            String feature = featureName.toString();
            output = output.append(whitespace).append(feature).append(": ")
                           .append(toString(thiz.delta(featureName),
                                            indent
                                            + indent(3 + feature.length()),
                                            thiz.getType()
                                                .appropType(featureName)));
            whitespace = indent;
        }
        return output.toString();
    }

    public String untaggedToString(Node thiz, String indent, Type defaultType) {
        StringBuffer output = new StringBuffer();
        Type nodetype = thiz.getType();
        if (nodetype instanceof ListType
                    && ((ListType) nodetype).getSubtype() != ListType.LIST) {
            output = output.append("<")
                           .append(listToString(thiz, indent, defaultType))
                           .append(" >");
        } else if (nodetype instanceof BasicType) {
            output.append(innerToString(thiz, indent, defaultType));
        } else {
            output.append("[").append(innerToString(thiz, indent, defaultType))
                  .append("]");
        }
        return output.toString();
    }

    public final static String indent(int depth) {
        StringBuffer space = new StringBuffer(depth + 1);
        for (int i = 0; i < depth; ++i) {
            space.append(' ');
        }
        return space.toString();
    }
}