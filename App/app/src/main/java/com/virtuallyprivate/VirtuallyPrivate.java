package com.virtuallyprivate;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.hardware.Camera;
import android.hardware.camera2.CameraDevice;
import android.location.Location;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaSyncEvent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.net.Uri;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class VirtuallyPrivate implements IXposedHookLoadPackage {
    final static String NAME = "VirtuallyPrivate";

    Context m_context;
    DatabaseManager m_dbManager;
    XSharedPreferences m_pref;
    NotificationManager m_notificationsManager;

    private void init(XC_LoadPackage.LoadPackageParam lpparam) {
        m_pref = new XSharedPreferences(MainActivity.class.getPackage().getName(), VirtuallyPrivate.NAME);
        m_context = (Context) XposedHelpers.callMethod( XposedHelpers.callStaticMethod( XposedHelpers.findClass("android.app.ActivityThread", lpparam.classLoader), "currentActivityThread"), "getSystemContext" );
        m_dbManager = new DatabaseManager(m_context);

        // init notifications
        NotificationChannel notificationsChannel = new NotificationChannel(NAME, "Restrictions", NotificationManager.IMPORTANCE_HIGH);
        m_notificationsManager = (NotificationManager) m_context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(m_notificationsManager.getNotificationChannel(NAME) == null) {
            m_notificationsManager.createNotificationChannel(notificationsChannel);
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log("Loaded VirtuallyPrivate");

        if(m_dbManager == null) {
            init(lpparam);
        }
        this._hookPermissions(lpparam);
    }

    private void _hookPermissions(XC_LoadPackage.LoadPackageParam lpparam) {
        // Clipboard
        if (m_dbManager.didUserRestrict(Permissions.CLIPBOARD, lpparam.packageName)) {
            findAndHookMethod(ClipData.class, "getItemAt", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
                    _showNotification(Permissions.CLIPBOARD, lpparam.appInfo);
                    m_pref.reload();
                    param.setResult(new ClipData.Item(m_pref.getString(SharedPrefs.CLIPBOARD, "")));
                }
            });
        }

        // App list
        if (m_dbManager.didUserRestrict(Permissions.APP_LIST, lpparam.packageName)) {
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    _showNotification(Permissions.APP_LIST, lpparam.appInfo);
                    param.setResult(new ArrayList<ApplicationInfo>());
                }
            };

            findAndHookMethod("android.app.ApplicationPackageManager", lpparam.classLoader,
                    "getInstalledApplications", int.class, hook);
            findAndHookMethod("android.app.ApplicationPackageManager", lpparam.classLoader,
                    "getInstalledPackages", int.class, hook);
            findAndHookMethod("android.content.pm.PackageManager", lpparam.classLoader,
                    "getInstalledApplications", int.class, hook);
            findAndHookMethod("android.content.pm.PackageManager", lpparam.classLoader,
                    "getInstalledPackages", int.class, hook);
        }

        // Camera
        if (m_dbManager.didUserRestrict(Permissions.CAMERA, lpparam.packageName)) {
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    _showNotification(Permissions.CAMERA, lpparam.appInfo);
                    param.setThrowable(new RuntimeException("CAMERA ERROR"));
                }
            };
            findAndHookMethod("android.hardware.camera2.CameraManager", lpparam.classLoader, "openCamera", String.class, CameraDevice.StateCallback.class, Handler.class, hook);
            findAndHookMethod("android.hardware.camera2.CameraManager", lpparam.classLoader, "openCamera", String.class, Executor.class, CameraDevice.StateCallback.class, hook);
            findAndHookMethod("android.hardware.Camera", lpparam.classLoader, "getCameraInfo", int.class, Camera.CameraInfo.class, hook);
            findAndHookMethod("android.hardware.Camera", lpparam.classLoader, "open", hook);
            findAndHookMethod("android.hardware.Camera", lpparam.classLoader, "open", int.class, hook);
        }

        // Location
        if (m_dbManager.didUserRestrict(Permissions.LOCATION, lpparam.packageName)) {
            findAndHookMethod(Location.class, "getLatitude", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    _showNotification(Permissions.LOCATION, lpparam.appInfo);
                    m_pref.reload();
                    double lat;
                    try {
                        lat = Double.parseDouble(m_pref.getString(SharedPrefs.Location.LATITUDE, "0.0"));
                    } catch (NumberFormatException _e) {
                       lat = 0.0;
                    }
                    param.setResult(lat);
                }
            });
            findAndHookMethod(Location.class, "getLongitude", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    _showNotification(Permissions.LOCATION, lpparam.appInfo);
                    m_pref.reload();
                    double lng;
                    try {
                        lng = Double.parseDouble(m_pref.getString(SharedPrefs.Location.LONGITUDE, "0.0"));
                    } catch (NumberFormatException _e) {
                        lng = 0.0;
                    }
                    param.setResult(lng);
                }
            });
        }

        // Microphone
        if(m_dbManager.didUserRestrict(Permissions.MICROPHONE, lpparam.packageName)) {
            XC_MethodReplacement mediaHook = this._createReturnNullHookReplacement(Permissions.MICROPHONE, lpparam.appInfo);
            findAndHookMethod(AudioRecord.class,"startRecording", MediaSyncEvent.class, mediaHook);
            findAndHookMethod(AudioRecord.class,"startRecording", mediaHook);
            findAndHookMethod(MediaRecorder.class, "setAudioSource", int.class, mediaHook);
        }
        
        // Contact list
        if (m_dbManager.didUserRestrict(Permissions.CONTACTS_LIST, lpparam.packageName)) {
            _showNotification(Permissions.CONTACTS_LIST, lpparam.appInfo);
            this._hookContentResolverQuery(lpparam, Permissions.CONTACTS_LIST, ContactsContract.Contacts.CONTENT_URI);
        }

        // call log
        if (m_dbManager.didUserRestrict(Permissions.CALL_LOG, lpparam.packageName)) {
            _showNotification(Permissions.CALL_LOG, lpparam.appInfo);
            this._hookContentResolverQuery(lpparam, Permissions.CALL_LOG, CallLog.Calls.CONTENT_URI);
        }
    }

    private void _hookContentResolverQuery(XC_LoadPackage.LoadPackageParam lpparam, String permission, Uri uriToBlock) {
        XC_MethodHook uriHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args[0] == uriToBlock) {
                    _showNotification(permission, lpparam.appInfo);
                    param.setResult(Constants.emptyCursor);
                }
            }
        };
        findAndHookMethod("android.content.ContentResolver", lpparam.classLoader, "query", Uri.class,
                String[].class, Bundle.class, CancellationSignal.class, uriHook);
        findAndHookMethod("android.content.ContentResolver", lpparam.classLoader, "query", Uri.class,
                String[].class, String.class, String[].class, String.class, uriHook);
        findAndHookMethod("android.content.ContentResolver", lpparam.classLoader, "query", Uri.class,
                String[].class, String.class, String[].class, String.class, CancellationSignal.class, uriHook);
    }

    private XC_MethodReplacement _createReturnNullHookReplacement(String permission, ApplicationInfo appInfo) {
        return new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) {
                _showNotification(permission, appInfo);
                return null;
            }
        };
    }

    private void _showNotification(String Permission, ApplicationInfo appInfo) {
        // Create a notification and set the notification channel.
        Notification notification = new Notification.Builder(m_context, NAME)
                .setContentTitle("VirtuallyPrivate")
                .setContentText(Utils.getAppLabel(m_context, appInfo)+ " tried to use: " + Permission + ".")
                .setChannelId(NAME)
                .build();
        // Issue the notification.
        m_notificationsManager.notify(1 , notification);
    }
}