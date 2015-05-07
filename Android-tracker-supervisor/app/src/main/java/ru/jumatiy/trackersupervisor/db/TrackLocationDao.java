package ru.jumatiy.trackersupervisor.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import ru.jumatiy.trackersupervisor.model.TrackLocation;
import uz.droid.orm.GenericDao;

import java.lang.reflect.Field;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 01.05.2015 10:43.
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
