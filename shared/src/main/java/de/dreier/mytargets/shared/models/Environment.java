/*
 * MyTargets Archery
 *
 * Copyright (C) 2015 Florian Dreier
 * All rights reserved
 */
package de.dreier.mytargets.shared.models;

import android.content.ContentValues;
import android.database.Cursor;

public class Environment extends IdProvider implements DatabaseSerializable {
    static final long serialVersionUID = 60L;
    public EWeather weather;
    public int windSpeed;
    public int windDirection;
    public String location;
    public static final String WEATHER = "weather";
    public static final String WIND_SPEED = "wind_speed";
    public static final String WIND_DIRECTION = "wind_direction";
    public static final String LOCATION = "location";

    public Environment() {

    }

    public Environment(EWeather weather, int windSpeed, int windDirection) {
        this.weather = weather;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
    }

    @Override
    public String getTableName() {
        return null;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(WEATHER, weather.getValue());
        values.put(WIND_SPEED, windSpeed);
        values.put(WIND_DIRECTION, windDirection);
        values.put(LOCATION, location);
        return values;
    }

    @Override
    public void fromCursor(Cursor cursor) {
        weather = EWeather.getOfValue(cursor.getInt(12));
        windSpeed = cursor.getInt(13);
        windDirection = cursor.getInt(14);
        location = cursor.getString(15);
    }
}
