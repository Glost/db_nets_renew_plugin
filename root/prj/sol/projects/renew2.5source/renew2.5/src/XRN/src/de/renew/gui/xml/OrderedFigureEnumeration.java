package de.renew.gui.xml;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.FigureWithID;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;


public class OrderedFigureEnumeration implements FigureEnumeration {
    private Enumeration<Figure> sorted;

    public OrderedFigureEnumeration(Enumeration<Figure> enumeration) {
        Vector<Figure> list = new Vector<Figure>();
        while (enumeration.hasMoreElements()) {
            list.add(enumeration.nextElement());
        }
        Collections.sort(list,
                         new Comparator<Figure>() {
                public int compare(Figure a, Figure b) {
                    if (a instanceof FigureWithID && b instanceof FigureWithID) {
                        int ida = ((FigureWithID) a)
                            .getID();
                        int idb = ((FigureWithID) b).getID();
                        if (ida < idb) {
                            return -1;
                        } else if (idb < ida) {
                            return 1;
                        } else {
                            return 0;
                        }
                    } else if (a instanceof FigureWithID) {
                        return -1;
                    } else if (b instanceof FigureWithID) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });
        sorted = list.elements();
    }

    public boolean hasMoreElements() {
        return sorted.hasMoreElements();
    }

    public Figure nextElement() {
        return sorted.nextElement();
    }

    public Figure nextFigure() {
        return nextElement();
    }
}