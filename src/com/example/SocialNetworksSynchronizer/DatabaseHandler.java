package com.example.SocialNetworksSynchronizer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String DB_NAME = "synchronized_contacts_db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_NAME = "contacts";
    public static final String ID_COLUMN = "contact_number";
    public static final String CONTACT = "synchronized_contact";

    private final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                                            ID_COLUMN + " INTEGER PRIMARY KEY," +
                                            CONTACT + " BLOB)" ;

    DatabaseHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {

    }

    public void dropTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME );
        db.execSQL(SQL_CREATE_TABLE);
    }
}
