package de.renew.util;

import java.util.LinkedList;


class SchedulerPair {
    final Long key;
    final LinkedList<Runnable> list;

    SchedulerPair(Long key) {
        this.key = key;
        list = new LinkedList<Runnable>();
    }
}