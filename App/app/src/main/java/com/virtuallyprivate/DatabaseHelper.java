package com.virtuallyprivate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import static com.virtuallyprivate.Database.COLUMN_ID;
import static com.virtuallyprivate.Database.PERMISSIONS_COLUMN_NAME;
import static com.virtuallyprivate.Database.PERMISSIONS_TABLE_NAME;
import static com.virtuallyprivate.Database.RESTRICTIONS_TABLE_NAME;
import static com.virtuallyprivate.Database.RESTRICTION_COLUMN_PACKAGE_ID;
import static com.virtuallyprivate.Database.RESTRICTION_COLUMN_PERMISSION_ID;

class DatabaseHelper {

    private Context m_context;
    private HashMap<String, Integer> m_permissions; // caching id-permissions.

    public DatabaseHelper(Context context) {
        this.m_context = context;
        this.m_permissions = new HashMap<>();
    }

    public void addPermission(Permission permission) {
        final ContentValues cv = new ContentValues();
        cv.put(PERMISSIONS_COLUMN_NAME, permission.getName());
        m_context.getContentResolver().insert(DataProvider.PERMISSIONS_URI, cv);
        Log.d("VP", "Inserted: " + permission.getName());
    }
    public void addAppRestriction(Restriction restriction) {
        final ContentValues cv = new ContentValues();
        cv.put(RESTRICTION_COLUMN_PACKAGE_ID, restriction.getPackageId());
        cv.put(RESTRICTION_COLUMN_PERMISSION_ID, restriction.getPermissionId());
        m_context.getContentResolver().insert(DataProvider.RESTRICTIONS_URI, cv);
        Log.d("VP", "Inserted: " + restriction.getPackageId());
    }

    public void deleteAppRestriction(Restriction restriction) {
        final String selection = COLUMN_ID + "= " + String.valueOf(restriction.getId(this));
        m_context.getContentResolver().delete(DataProvider.RESTRICTIONS_URI, selection, null);
        Log.d("VP", "Deleted: " + restriction.getPackageId());
    }

    private int _getPrimaryKey(String tableName, String selectionQuery) {
        return DatabaseHelper._getPrimaryKey(m_context, tableName, selectionQuery);
    }

    private static int _getPrimaryKey(Context context, String tableName, String selectionQuery) {
        final String[] projection = { COLUMN_ID };

        Uri uri = tableName.equals(PERMISSIONS_TABLE_NAME) ?
                DataProvider.PERMISSIONS_URI : DataProvider.RESTRICTIONS_URI;
        final Cursor cursor = context.getContentResolver().query(
                uri,
                projection,
                selectionQuery,null,null);

        while(cursor != null && cursor.moveToNext()) {
            final int primaryKey = cursor.getInt(
                    cursor.getColumnIndexOrThrow(COLUMN_ID));
            cursor.close();
            return primaryKey;
        }
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
    public static int getPermissionPrimaryKey(Context context, String permissionName) {
       return DatabaseHelper._getPrimaryKey(context, PERMISSIONS_TABLE_NAME, PERMISSIONS_COLUMN_NAME + "='" + permissionName + "'");
    }

    public int getRestrictionPrimaryKey(String packageId, int permissionId) {
        return this._getPrimaryKey(RESTRICTIONS_TABLE_NAME,
                RESTRICTION_COLUMN_PACKAGE_ID + "='" + packageId + "' AND " + RESTRICTION_COLUMN_PERMISSION_ID + "='" + permissionId + "'");
    }

    public ArrayList<String> getAvailablePermissions() {
        final String[] projection = {
                PERMISSIONS_COLUMN_NAME
        };

        final Cursor cursor = m_context.getContentResolver().query(
                DataProvider.PERMISSIONS_URI,
                projection,
                null,null,null
        );

        final ArrayList<String> permissions = new ArrayList<String>();
        if (cursor != null) {
            while(cursor.moveToNext()) {
                final String permission = cursor.getString(
                        cursor.getColumnIndexOrThrow(PERMISSIONS_COLUMN_NAME));
                permissions.add(permission);
            }
            cursor.close();
        }
        return permissions;
    }

    public static boolean didUserRestrict(Context context, String permission, String packageId) {
        boolean didRestrict = false;
        final String selectionQuery = RESTRICTION_COLUMN_PERMISSION_ID + "=" + DatabaseHelper.getPermissionPrimaryKey(context, permission) + " AND " + RESTRICTION_COLUMN_PACKAGE_ID + "='" + packageId + "' ";
        final String[] projection = {
                COLUMN_ID
        };

        Cursor cursor = context.getContentResolver().query(
                    DataProvider.RESTRICTIONS_URI,
                    projection,
                    selectionQuery,null," LIMIT 1"
            );
        while(cursor != null && cursor.moveToNext()) {
            didRestrict = true;
            break;
        }
        if ( cursor != null ) {
            cursor.close();
        }
        return didRestrict;
    }

    public boolean didUserRestrict(String permission, String packageId) {
        return DatabaseHelper.didUserRestrict(m_context, permission, packageId);
    }
}
