package ru.jumatiy.tracker.orm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 11.04.2015 20:16.
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {

    private static String NAME = "tracker.db";
    private static int VERSION = 2;

    private Context mContext;

    public DatabaseOpenHelper(Context context) {
        super(context, NAME, null, VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(new TrackLocationDao(mContext).getOnCreateSQL());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(new TrackLocationDao(mContext).getDropSQL());
        db.execSQL(new TrackLocationDao(mContext).getOnCreateSQL());
    }

}
