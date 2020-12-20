package com.virtuallyprivate;

import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.location.Location;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaSyncEvent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.net.Uri;
import java.util.ArrayList;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class VirtuallyPrivate implements IXposedHookLoadPackage {
    DatabaseManager m_dbManager;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log("IN VIRTUALLYPRIVATE");

        if(m_dbManager == null) {
            Context systemContext = (Context) XposedHelpers.callMethod( XposedHelpers.callStaticMethod( XposedHelpers.findClass("android.app.ActivityThread", lpparam.classLoader), "currentActivityThread"), "getSystemContext" );
            m_dbManager = new DatabaseManager(systemContext);
        }
        this._hookPermissions(lpparam);
    }

    private void _hookPermissions(XC_LoadPackage.LoadPackageParam lpparam) {
        XC_MethodReplacement mediaHook = new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                return null;
            }
        };

        // Clipboard
        if (m_dbManager.didUserRestrict(Permissions.CLIPBOARD, lpparam.packageName)) {
            findAndHookMethod(ClipData.class, "getItemAt", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
                    param.setResult(new ClipData.Item(""));
                }
            });
        }

        // App list
        if (m_dbManager.didUserRestrict(Permissions.APP_LIST, lpparam.packageName)) {
            findAndHookMethod("android.app.ApplicationPackageManager", lpparam.classLoader, "getInstalledApplications",
            int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(new ArrayList<ApplicationInfo>());
                }
            });
        }

        // Camera
        if (m_dbManager.didUserRestrict(Permissions.CAMERA, lpparam.packageName)) {
            findAndHookMethod("android.hardware.Camera", lpparam.classLoader, "getCameraInfo", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.setThrowable(new RuntimeException("CAMERA ERROR"));
                }
            });
            findAndHookMethod("android.hardware.camera2.CameraManager", lpparam.classLoader, "openCamera", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.setThrowable(new SecurityException("CAMERA ERROR"));
                }
            });
        }

        // Location
        if (m_dbManager.didUserRestrict(Permissions.LOCATION, lpparam.packageName)) {
            findAndHookMethod(Location.class, "getLatitude", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    double lat = 0.0;
                    param.setResult(lat);
                }
            });
            findAndHookMethod(Location.class, "getLongitude", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    double lng = 0.0;
                    param.setResult(lng);
                }
            });
        }

        // Microphone
        if(m_dbManager.didUserRestrict(Permissions.MICROPHONE, lpparam.packageName)) {
            findAndHookMethod(AudioRecord.class,"startRecording", MediaSyncEvent.class, mediaHook);
            findAndHookMethod(AudioRecord.class,"startRecording", mediaHook);
            findAndHookMethod(MediaRecorder.class, "setAudioSource", int.class, mediaHook);
        }
        
        // Contact list
        if (m_dbManager.didUserRestrict(Permissions.CONTACTS_LIST, lpparam.packageName)) {
            VirtuallyPrivate._hookContentResolverQuery(lpparam, ContactsContract.Contacts.CONTENT_URI);
        }

        // call log
        if (m_dbManager.didUserRestrict(Permissions.CALL_LOG, lpparam.packageName)) {
            VirtuallyPrivate._hookContentResolverQuery(lpparam, CallLog.Calls.CONTENT_URI);
        }
    }

    private static void _hookContentResolverQuery(XC_LoadPackage.LoadPackageParam lpparam, Uri uriToBlock) {
        Cursor emptyCursor = new Cursor() {
            @Override
            public int getCount() {
                return 0;
            }

            @Override
            public int getPosition() {
                return 0;
            }

            @Override
            public boolean move(int offset) {
                return false;
            }

            @Override
            public boolean moveToPosition(int position) {
                return false;
            }

            @Override
            public boolean moveToFirst() {
                return false;
            }

            @Override
            public boolean moveToLast() {
                return false;
            }

            @Override
            public boolean moveToNext() {
                return false;
            }

            @Override
            public boolean moveToPrevious() {
                return false;
            }

            @Override
            public boolean isFirst() {
                return false;
            }

            @Override
            public boolean isLast() {
                return false;
            }

            @Override
            public boolean isBeforeFirst() {
                return false;
            }

            @Override
            public boolean isAfterLast() {
                return false;
            }

            @Override
            public int getColumnIndex(String columnName) {
                return 0;
            }

            @Override
            public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
                return 0;
            }

            @Override
            public String getColumnName(int columnIndex) {
                return null;
            }

            @Override
            public String[] getColumnNames() {
                return new String[0];
            }

            @Override
            public int getColumnCount() {
                return 0;
            }

            @Override
            public byte[] getBlob(int columnIndex) {
                return new byte[0];
            }

            @Override
            public String getString(int columnIndex) {
                return null;
            }

            @Override
            public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {

            }

            @Override
            public short getShort(int columnIndex) {
                return 0;
            }

            @Override
            public int getInt(int columnIndex) {
                return 0;
            }

            @Override
            public long getLong(int columnIndex) {
                return 0;
            }

            @Override
            public float getFloat(int columnIndex) {
                return 0;
            }

            @Override
            public double getDouble(int columnIndex) {
                return 0;
            }

            @Override
            public int getType(int columnIndex) {
                return 0;
            }

            @Override
            public boolean isNull(int columnIndex) {
                return false;
            }

            @Override
            public void deactivate() {

            }

            @Override
            public boolean requery() {
                return false;
            }

            @Override
            public void close() {

            }

            @Override
            public boolean isClosed() {
                return false;
            }

            @Override
            public void registerContentObserver(ContentObserver observer) {

            }

            @Override
            public void unregisterContentObserver(ContentObserver observer) {

            }

            @Override
            public void registerDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public void unregisterDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public void setNotificationUri(ContentResolver cr, Uri uri) {

            }

            @Override
            public Uri getNotificationUri() {
                return null;
            }

            @Override
            public boolean getWantsAllOnMoveCalls() {
                return false;
            }

            @Override
            public void setExtras(Bundle extras) {

            }

            @Override
            public Bundle getExtras() {
                return null;
            }

            @Override
            public Bundle respond(Bundle extras) {
                return null;
            }
        };
        XC_MethodHook contactshook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args[0] == uriToBlock) {
                    param.setResult(emptyCursor);
                }
            }
        };
        findAndHookMethod("android.content.ContentResolver", lpparam.classLoader, "query", Uri.class,
                String[].class, Bundle.class, CancellationSignal.class, contactshook);
        findAndHookMethod("android.content.ContentResolver", lpparam.classLoader, "query", Uri.class,
                String[].class, String.class, String[].class, String.class, contactshook);
        findAndHookMethod("android.content.ContentResolver", lpparam.classLoader, "query", Uri.class,
                String[].class, String.class, String[].class, String.class, CancellationSignal.class, contactshook);
    }
}