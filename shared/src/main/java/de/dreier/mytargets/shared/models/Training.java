package de.dreier.mytargets.shared.models;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Calendar;
import java.util.Date;

public class Training extends IdProvider implements DatabaseSerializable {
    static final long serialVersionUID = 58L;
    public static final String TABLE = "TRAINING";
    public static final String TITLE = "title";
    public static final String DATE = "datum";
    public static final String INDOOR = "indoor";
    //TODO evtl. save standard round id here
    public static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE + " ( " +
                    ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DATE + " INTEGER," +
                    TITLE + " TEXT," +
                    INDOOR + " BOOLEAN,"+
                    Environment.WEATHER + " INTEGER," +
                    Environment.WIND_SPEED + " INTEGER," +
                    Environment.WIND_DIRECTION + " INTEGER," +
                    Environment.LOCATION + " TEXT);";

    public String title = "";
    public Date date = new Date();
    public int reachedPoints;
    public int maxPoints;
    public int[] scoreCount = new int[3];
    public boolean indoor;
    public Environment environment;

    @Override
    public long getParentId() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(0);
        c.set(date.getYear() + 1900, date.getMonth(), 1);
        return c.getTimeInMillis();
    }

    @Override
    public String getTableName() {
        return TABLE;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(TITLE, title);
        values.put(DATE, date.getTime());
        values.put(INDOOR, indoor);
        values.putAll(environment.getContentValues());
        return values;
    }

    @Override
    public void fromCursor(Cursor cursor) {
        id = cursor.getLong(0);
        title = cursor.getString(1);
        date = new Date(cursor.getLong(2));
        reachedPoints = cursor.getInt(3);
        maxPoints = cursor.getInt(4);
        if (maxPoints <= 10) {
            maxPoints = 0;
        }
        indoor = cursor.getInt(5)==0;
        environment = new Environment();
        environment.fromCursor(cursor);
    }
}
