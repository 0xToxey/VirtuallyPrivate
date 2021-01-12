package com.virtuallyprivate;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {
    public static final String PERMISSIONS_TABLE_NAME = "PERMISSIONS";
    public static final String RESTRICTIONS_TABLE_NAME = "RESTRICTIONS";
    public static final String PERMISSIONS_COLUMN_NAME = "NAME";
    public static final String COLUMN_ID = "ID";
    public static final String RESTRICTION_COLUMN_PACKAGE_ID = "PACKAGE_ID";
    public static final String RESTRICTION_COLUMN_PERMISSION_ID = "PERMISSION_ID";

    public static final String[] PERMISSIONS_ALL_COLUMNS =
            {COLUMN_ID, PERMISSIONS_COLUMN_NAME};
    public static final String[] RESTRICTIONS_ALL_COLUMNS =
            {COLUMN_ID, RESTRICTION_COLUMN_PACKAGE_ID, RESTRICTION_COLUMN_PERMISSION_ID};


    public Database(Context context) {
        super(context, "virtuallyPrivate.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTableStatement = "CREATE TABLE " + PERMISSIONS_TABLE_NAME + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + PERMISSIONS_COLUMN_NAME + " TEXT UNIQUE )";
        sqLiteDatabase.execSQL(createTableStatement);

        createTableStatement = "CREATE TABLE " + RESTRICTIONS_TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RESTRICTION_COLUMN_PACKAGE_ID + " TEXT NOT NULL, " +
                RESTRICTION_COLUMN_PERMISSION_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + RESTRICTION_COLUMN_PERMISSION_ID + ") REFERENCES "+ PERMISSIONS_TABLE_NAME + "("+ COLUMN_ID + "));";
        sqLiteDatabase.execSQL(createTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {}
}
