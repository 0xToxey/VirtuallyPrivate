package com.virtuallyprivate;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DataProvider extends ContentProvider {

    private static final String AUTHORITY = BuildConfig.APPLICATION_ID;
    public static final Uri PERMISSIONS_URI = Uri.parse("content://" + AUTHORITY + "/" + Database.PERMISSIONS_TABLE_NAME);
    public static final Uri RESTRICTIONS_URI = Uri.parse("content://" + AUTHORITY + "/" + Database.RESTRICTIONS_TABLE_NAME);
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int PERMISSIONS = 1;
    private static final int PERMISSIONS_ID = 2;
    private static final int RESTRICTIONS = 3;
    private static final int RESTRICTIONS_ID = 4;
    static {
        uriMatcher.addURI(AUTHORITY, Database.PERMISSIONS_TABLE_NAME, PERMISSIONS);
        uriMatcher.addURI(AUTHORITY, Database.PERMISSIONS_TABLE_NAME + "/#", PERMISSIONS_ID);
        uriMatcher.addURI(AUTHORITY, Database.RESTRICTIONS_TABLE_NAME, RESTRICTIONS);
        uriMatcher.addURI(AUTHORITY, Database.RESTRICTIONS_TABLE_NAME + "/#", RESTRICTIONS_ID);
    }
    private SQLiteDatabase m_database;

    public void _securityCheck() {
        if (!Utils.getProcessNameForPid(getContext(), Binder.getCallingPid()).equals(BuildConfig.APPLICATION_ID)) {
            throw new SecurityException();
        }
    }

    @Override
    public boolean onCreate() {
        Database helper = new Database(getContext());
        m_database = helper.getWritableDatabase();
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        Cursor cursor;
        switch (uriMatcher.match(uri)) {
            case PERMISSIONS:
                cursor = m_database.query(Database.PERMISSIONS_TABLE_NAME, Database.PERMISSIONS_ALL_COLUMNS,
                        s, null, null, null, null);
                break;
            case RESTRICTIONS:
                cursor = m_database.query(Database.RESTRICTIONS_TABLE_NAME, Database.RESTRICTIONS_ALL_COLUMNS,
                        s, null, null, null, null);
                break;
            default:
                throw new IllegalArgumentException("This is an Unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case PERMISSIONS:
                return "vnd.android.cursor.dir/" + Database.PERMISSIONS_TABLE_NAME;
            case RESTRICTIONS:
                return "vnd.android.cursor.dir/" + Database.RESTRICTIONS_TABLE_NAME;
            default:
                throw new IllegalArgumentException("This is an Unknown URI " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        _securityCheck();
        long id;
        Uri _uri;
        switch (uriMatcher.match(uri)) {
            case PERMISSIONS:
                id = m_database.insert(Database.PERMISSIONS_TABLE_NAME, null, contentValues);
                _uri = ContentUris.withAppendedId(PERMISSIONS_URI, id);
                getContext().getContentResolver().notifyChange(_uri, null);
                return _uri;
            case RESTRICTIONS:
                id = m_database.insert(Database.RESTRICTIONS_TABLE_NAME, null, contentValues);
                _uri = ContentUris.withAppendedId(RESTRICTIONS_URI, id);
                getContext().getContentResolver().notifyChange(_uri, null);
                return _uri;
            default:
                throw new IllegalArgumentException("This is an Unknown URI " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        _securityCheck();
        int delCount = 0;
        switch (uriMatcher.match(uri)) {
            case PERMISSIONS:
                delCount = m_database.delete(Database.PERMISSIONS_TABLE_NAME, s, strings);
                break;
            case RESTRICTIONS:
                delCount = m_database.delete(Database.RESTRICTIONS_TABLE_NAME, s, strings);
                break;
            default:
                throw new IllegalArgumentException("This is an Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return delCount;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        _securityCheck();
        int updCount = 0;
        switch (uriMatcher.match(uri)) {
            case PERMISSIONS:
                updCount = m_database.update(Database.PERMISSIONS_TABLE_NAME, contentValues, s, strings);
                break;
            case RESTRICTIONS:
                updCount = m_database.update(Database.RESTRICTIONS_TABLE_NAME, contentValues, s, strings);
                break;
            default:
                throw new IllegalArgumentException("This is an Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return updCount;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values, @Nullable Bundle extras) {
        _securityCheck();
        return super.insert(uri, values, extras);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable Bundle extras) {
        _securityCheck();
        return super.delete(uri, extras);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable Bundle extras) {
        _securityCheck();
        return super.update(uri, values, extras);
    }
}