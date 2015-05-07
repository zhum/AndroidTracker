package ru.jumatiy.tracker.orm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import ru.jumatiy.tracker.model.TrackLocation;
import uz.droid.orm.GenericDao;

import java.lang.reflect.Field;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 24.03.2015 11:25.
 */

public class TrackLocationDao extends GenericDao<TrackLocation, Long> {

    public TrackLocationDao(Context context) {
        super(context, TrackLocation.class);
    }

    @Override
    protected void putOtherTypeToCV(Object obj, Field f, String columnName, ContentValues cv) {

    }

    @Override
    protected void setOtherTypeField(Object obj, Field f, String columnName, Cursor cursor) {

    }

    @Override
    protected SQLiteDatabase getReadableDB() {
        return new DatabaseOpenHelper(getContext()).getReadableDatabase();
    }

    @Override
    protected SQLiteDatabase getWritableDB() {
        return new DatabaseOpenHelper(getContext()).getWritableDatabase();
    }
}
