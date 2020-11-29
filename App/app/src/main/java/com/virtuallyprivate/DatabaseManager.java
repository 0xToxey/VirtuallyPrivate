package com.virtuallyprivate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.Nullable;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager extends SQLiteOpenHelper {
    public static final String PERMISSIONS_TABLE_NAME = "PERMISSIONS";
    public static final String RESTRICTIONS_TABLE_NAME = "RESTRICTIONS";
    public static final String PERMISSIONS_COLUMN_NAME = "NAME";
    public static final String COLUMN_ID = "ID";
    public static final String RESTRICTION_COLUMN_PACKAGE_ID = "PACKAGE_ID";
    public static final String RESTRICTION_COLUMN_PERMISSION_ID = "PERMISSION_ID";

    private HashMap<String, Integer> m_permissions; // caching id-permissions.

    public DatabaseManager(@Nullable Context context) {
        super(context, "virtuallyPrivate.db", null, 1);
        this.m_permissions = new HashMap<>();
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
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void addPermission(Permission permission) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(PERMISSIONS_COLUMN_NAME, permission.getName());
        long a = db.insert(PERMISSIONS_TABLE_NAME, null, cv);

        Log.d("VP", "Inserted: " + permission.getName() + " code: " + a);
    }
    public void addAppRestriction(Restriction restriction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(RESTRICTION_COLUMN_PACKAGE_ID, restriction.getPackageId());
        cv.put(RESTRICTION_COLUMN_PERMISSION_ID, restriction.getPermissionId());
        long a = db.insert(RESTRICTIONS_TABLE_NAME, null, cv);

        Log.d("VP", "Inserted: " + restriction.getPackageId() + " code: " + a);
    }

    public void deleteAppRestriction(Restriction restriction) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_ID + "= " + String.valueOf(restriction.getId(this));
        // Issue SQL statement.
        int rows = db.delete(RESTRICTIONS_TABLE_NAME, selection, null);
        Log.d("VP", "Deleted: " + restriction.getPackageId() + " rows: " + rows);
    }

    private int _getPrimaryKey(String tableName, String selectionQuery) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT ID FROM " + tableName + " WHERE " + selectionQuery, null);
        while(cursor.moveToNext()) {
            int primaryKey = cursor.getInt(
                    cursor.getColumnIndexOrThrow(COLUMN_ID));
            cursor.close();
            return primaryKey;
        }
        cursor.close();
        return -1;
    }

    public int getPermissionPrimaryKey(String permissionName) {
        int permissionId = m_permissions.getOrDefault(permissionName, -1);
        if(permissionId == -1) { // caching.
            permissionId = this._getPrimaryKey(PERMISSIONS_TABLE_NAME, PERMISSIONS_COLUMN_NAME + "='" + permissionName + "'");
            m_permissions.put(permissionName, permissionId);
        }
        return permissionId;
    }

    public int getRestrictionPrimaryKey(String packageId, int permissionId) {
        return this._getPrimaryKey(RESTRICTIONS_TABLE_NAME,
                RESTRICTION_COLUMN_PACKAGE_ID + "='" + packageId + "' AND " + RESTRICTION_COLUMN_PERMISSION_ID + "='" + permissionId + "'");
    }

    public ArrayList<String> getAvailablePermissions() {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                PERMISSIONS_COLUMN_NAME
        };

        Cursor cursor = db.query(PERMISSIONS_TABLE_NAME,
                projection,null,null,null,null,""
        );

        ArrayList<String> permissions = new ArrayList<String>();
        while(cursor.moveToNext()) {
            String permission = cursor.getString(
                    cursor.getColumnIndexOrThrow(PERMISSIONS_COLUMN_NAME));
            permissions.add(permission);
        }
        cursor.close();

        return permissions;
    }
    public boolean didUserRestrict(String permission, String packageId) {
        boolean didRestrict = false;
        SQLiteDatabase db = this.getReadableDatabase();

        String selectionQuery = RESTRICTION_COLUMN_PERMISSION_ID + "=" + this.getPermissionPrimaryKey(permission) + " AND " + RESTRICTION_COLUMN_PACKAGE_ID + "='" + packageId + "' ";

        Cursor cursor = db.rawQuery("SELECT ID FROM " + RESTRICTIONS_TABLE_NAME + " WHERE " + selectionQuery + " LIMIT 1", null);
        while(cursor.moveToNext()) {
            didRestrict = true;
            break;
        }
        cursor.close();
        return didRestrict;
    }
}
