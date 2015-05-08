/*
 * MyTargets Archery
 *
 * Copyright (C) 2015 Florian Dreier
 * All rights reserved
 */
package de.dreier.mytargets.models;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class Distance extends IdProvider implements Comparable<Distance>, Serializable {
    static final long serialVersionUID = 50L;

    public int distance;
    public String unit;

    public Distance(int distance, String unit) {
        this.id = (distance << 1) | (unit.equals("m") ? 1 : 0);
        this.distance = distance;
        this.unit = unit;
    }

    @Override
    public int compareTo(@NonNull Distance another) {
        if (unit.equals(another.unit)) {
            return (int) (id - another.id);
        }
        return unit.compareTo(another.unit);
    }

    @Override
    public String toString() {
        return distance + unit;
    }

    public static Distance fromId(long id) {
        return new Distance((int) id >> 1, (id & 1) == 1 ? "m" : "yd");
    }
}
