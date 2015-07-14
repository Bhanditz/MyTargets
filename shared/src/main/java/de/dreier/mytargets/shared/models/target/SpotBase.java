/*
 * MyTargets Archery
 *
 * Copyright (C) 2015 Florian Dreier
 * All rights reserved
 */
package de.dreier.mytargets.shared.models.target;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.StringRes;

import de.dreier.mytargets.shared.models.Diameter;
import de.dreier.mytargets.shared.models.Dimension;

public class SpotBase extends Target {
    protected int faceRadius;
    protected Target face;
    protected int[][] facePositions;

    protected SpotBase(Context c, long id, @StringRes int nameRes) {
        super(c, id, nameRes);
    }

    @Override
    protected void draw(Canvas canvas, Rect rect) {
        for (int i=0;i<facePositions.length;i++) {
            face.draw(canvas, getBounds(i, rect));
        }
        onPostDraw(canvas, rect);
    }

    @Override
    public String zoneToString(int zone) {
        face.scoringStyle = scoringStyle;
        return face.zoneToString(zone);
    }

    public int getPointsByZone(int zone) {
        face.scoringStyle = scoringStyle;
        return face.getPointsByZone(zone);
    }

    public int getMaxPoints() {
        face.scoringStyle = scoringStyle;
        return face.getMaxPoints();
    }

    public float zoneToX(int zone) {
        face.scoringStyle = scoringStyle;
        return face.zoneToX(zone);
    }

    public int getZoneColor(int zone) {
        face.scoringStyle = scoringStyle;
        return face.getZoneColor(zone);
    }

    public int getStrokeColor(int zone) {
        face.scoringStyle = scoringStyle;
        return face.getStrokeColor(zone);
    }

    public int getTextColor(int zone) {
        face.scoringStyle = scoringStyle;
        return face.getTextColor(zone);
    }

    public int getZones() {
        return face.zones;
    }

    @Override
    public Diameter[] getDiameters(Context context) {
        return new Diameter[]{new Diameter(40, Dimension.CENTIMETER),
                new Diameter(60, Dimension.CENTIMETER)};
    }

    public Rect getBounds(int index, Rect rect) {
        int pos[] = facePositions[index];
        Rect bounds = new Rect();
        bounds.left = (int) (rect.left + recalc(rect,pos[0] - faceRadius));
        bounds.top = (int) (rect.top + recalc(rect,pos[1] - faceRadius));
        bounds.right = (int) (rect.left + recalc(rect,pos[0] + faceRadius));
        bounds.bottom = (int) (rect.top + recalc(rect,pos[1] + faceRadius));
        return bounds;
    }

    @Override
    public int getZoneFromPoint(float x, float y) {
        float ax = x * 500;
        float ay = y * 500;
        for (int i=0;i<facePositions.length;i++) {
            Rect bounds = getBounds(i, new Rect(0, 0, 500, 500));
            if(bounds.contains((int) ax, (int) ay)) {
                return face.getZoneFromPoint((ax-bounds.left)*bounds.width()/500,(ay-bounds.top)*bounds.height()/500);//TODO make more reliable
            }
        }
        return -1;
    }
}
