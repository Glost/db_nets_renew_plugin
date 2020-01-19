package de.uni_hamburg.fs;

import collections.CollectionEnumeration;
import collections.HashedMap;
import collections.LinkedList;
import collections.Seq;
import collections.UpdatableMap;
import collections.UpdatableSeq;


public class OrderedTable implements java.io.Serializable {
    // OK: These three used to be final, but that triggers the
    // all too well-known final bug.
    private UpdatableMap table;
    private UpdatableSeq keys;
    private UpdatableSeq orderSource;

    public OrderedTable() {
        this(null);
    }

    public OrderedTable(CollectionEnumeration orderEnum) {
        if (orderEnum == null) {
            orderSource = null;
        } else {
            orderSource = new LinkedList();
            while (orderEnum.hasMoreElements()) {
                orderSource.insertLast(orderEnum.nextElement());
            }
        }
        table = new HashedMap();
        keys = new LinkedList();
    }

    private OrderedTable(UpdatableMap table, UpdatableSeq keys,
                         UpdatableSeq orderSource) {
        this.table = (UpdatableMap) table.duplicate();
        this.keys = (UpdatableSeq) keys.duplicate();
        this.orderSource = orderSource; // not modified
    }

    public void putAt(Object key, Object value) {
        if (!keys.includes(key)) {
            if (orderSource == null) {
                keys.insertLast(key);
            } else {
                // find insert position according to orderSource:
                int index = 0;
                boolean found = false;
                for (int i = 0; i < orderSource.size(); ++i) {
                    Object currkey = orderSource.at(i);
                    if (key.equals(currkey)) {
                        found = true;
                        break;
                    }
                    if (index < keys.size() && keys.at(index).equals(currkey)) {
                        ++index;
                    }
                }
                if (found) {
                    keys.insertAt(index, key);
                } else {
                    keys.insertLast(key);
                }
            }
        }
        table.putAt(key, value);
    }

    public void insertAt(Object key, int index, Object value) {
        keys.removeOneOf(key);
        keys.insertAt(index, key);
        table.putAt(key, value);
    }

    public void removeAt(Object key) {
        keys.removeOneOf(key);
        table.removeAt(key);
    }

    public boolean includesKey(Object key) {
        return keys.includes(key);
    }

    public boolean includes(Object element) {
        return table.includes(element);
    }

    public int size() {
        return keys.size();
    }

    public CollectionEnumeration keys() {
        return ((Seq) keys.duplicate()).elements();
    }

    public Object at(Object key) {
        return table.at(key);
    }

    public boolean isEmpty() {
        return keys.isEmpty();
    }

    public Object duplicate() {
        return new OrderedTable(table, keys, orderSource);
    }
}