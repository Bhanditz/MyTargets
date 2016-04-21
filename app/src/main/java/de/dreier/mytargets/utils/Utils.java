/*
 * MyTargets Archery
 *
 * Copyright (C) 2015 Florian Dreier
 * All rights reserved
 */
package de.dreier.mytargets.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Utils {
    public static PackageInfo getAppVersionInfo(Context context) {
        PackageInfo pInfo;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return pInfo;
    }

    public static long getMonthId(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(0);
        c.set(date.getYear() + 1900, date.getMonth(), 1);
        return c.getTimeInMillis();
    }

    public static long[] toArray(List<Long> values) {
        long[] result = new long[values.size()];
        int i = 0;
        for (Long l : values)
            result[i++] = l;
        return result;
    }

    public static List<Long> toList(long[] array) {
        List<Long> list = new ArrayList<>();
        for (long value : array) {
            list.add(value);
        }
        return list;
    }
}
