package ru.jumatiy.trackersupervisor.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 01.05.2015 10:36.
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "database.db";
    public static final int DATABASE_VERSION = 2;

    private Context context;

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(new TrackLocationDao(context).getOnCreateSQL());
        db.execSQL(new DetectorPointDao(context).getOnCreateSQL());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            DetectorPointDao dao = new DetectorPointDao(context);
            db.execSQL(dao.getDropSQL());
            db.execSQL(dao.getOnCreateSQL());
        }
    }
}
