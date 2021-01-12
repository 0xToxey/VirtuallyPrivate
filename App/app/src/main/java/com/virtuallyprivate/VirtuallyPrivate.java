package com.virtuallyprivate;

import android.app.AndroidAppHelper;
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
import java.util.HashMap;
import java.util.HashSet;
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

    static Context m_systemContext;
    static XSharedPreferences m_pref;
    static NotificationManager m_notificationsManager;
    private HashMap<String, Boolean> m_isRestricted;  // for cache
    private HashMap<String, XC_MethodHook> m_permissionsHooks;

    private void _initPermissionsHooks(XC_LoadPackage.LoadPackageParam lpparam) {
        m_permissionsHooks = new HashMap<>();
        m_permissionsHooks.put(Permissions.CLIPBOARD, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
                if (_isRestricted(Permissions.CLIPBOARD, lpparam)) {
                    param.setResult(new ClipData.Item(m_pref.getString(SharedPrefs.CLIPBOARD, "")));
                }
            }
        });
        m_permissionsHooks.put(Permissions.CAMERA, _createReplacementHook(Permissions.CAMERA, lpparam, null));
    }

    private void _init(XC_LoadPackage.LoadPackageParam lpparam) {
        m_isRestricted = new HashMap<>();
        m_pref = new XSharedPreferences(MainActivity.class.getPackage().getName(), VirtuallyPrivate.NAME);
        m_systemContext = (Context) XposedHelpers.callMethod(
                XposedHelpers.callStaticMethod( XposedHelpers.findClass("android.app.ActivityThread",
                        lpparam.classLoader), "currentActivityThread"), "getSystemContext" );

        // init notifications
        NotificationChannel notificationsChannel = new NotificationChannel(NAME, "Restrictions", NotificationManager.IMPORTANCE_HIGH);
        m_notificationsManager = (NotificationManager) m_systemContext.getSystemService(Context.NOTIFICATION_SERVICE);
        m_notificationsManager.createNotificationChannel(notificationsChannel);

        _initPermissionsHooks(lpparam);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log("Loaded VirtuallyPrivate");
        _init(lpparam);
        _hookPermissions(lpparam);
    }

    private void _hookPermissions(XC_LoadPackage.LoadPackageParam lpparam) {
        // Clipboard
        findAndHookMethod(ClipData.class, "getItemAt", int.class, m_permissionsHooks.get(Permissions.CLIPBOARD));

        // App list
        findAndHookMethod("android.app.ApplicationPackageManager", lpparam.classLoader,
                "getInstalledApplications", int.class, _createAppListHook(lpparam, false));
        findAndHookMethod("android.app.ApplicationPackageManager", lpparam.classLoader,
                "getInstalledPackages", int.class, _createAppListHook(lpparam, true));
        findAndHookMethod("android.content.pm.PackageManager", lpparam.classLoader,
                "getInstalledApplications", int.class, _createAppListHook(lpparam, false));
        findAndHookMethod("android.content.pm.PackageManager", lpparam.classLoader,
                "getInstalledPackages", int.class, _createAppListHook(lpparam, true));

        // Camera
        findAndHookMethod("android.hardware.camera2.CameraManager", lpparam.classLoader, "openCamera", String.class, CameraDevice.StateCallback.class, Handler.class, m_permissionsHooks.get(Permissions.CAMERA));
        findAndHookMethod("android.hardware.camera2.CameraManager", lpparam.classLoader, "openCamera", String.class, Executor.class, CameraDevice.StateCallback.class, m_permissionsHooks.get(Permissions.CAMERA));
        findAndHookMethod("android.hardware.Camera", lpparam.classLoader, "getCameraInfo", int.class, Camera.CameraInfo.class, m_permissionsHooks.get(Permissions.CAMERA));
        findAndHookMethod("android.hardware.Camera", lpparam.classLoader, "open", m_permissionsHooks.get(Permissions.CAMERA));
        findAndHookMethod("android.hardware.Camera", lpparam.classLoader, "open", int.class, m_permissionsHooks.get(Permissions.CAMERA));

        // Location
        findAndHookMethod(Location.class, "getLatitude", _createLocationHook(lpparam,SharedPrefs.Location.LATITUDE));
        findAndHookMethod(Location.class, "getLongitude", _createLocationHook(lpparam,SharedPrefs.Location.LONGITUDE));

        // Microphone
        XC_MethodHook mediaHook = this._createReplacementHook(Permissions.MICROPHONE, lpparam, null);
        findAndHookMethod(AudioRecord.class,"startRecording", MediaSyncEvent.class, mediaHook);
        findAndHookMethod(AudioRecord.class,"startRecording", mediaHook);
        findAndHookMethod(MediaRecorder.class, "setAudioSource", int.class, mediaHook);
        
        // Contact list
        this._hookContentResolverQuery(lpparam, Permissions.CONTACTS_LIST, ContactsContract.Contacts.CONTENT_URI);

        // call log
        this._hookContentResolverQuery(lpparam, Permissions.CALL_LOG, CallLog.Calls.CONTENT_URI);
    }

    private void _hookContentResolverQuery(XC_LoadPackage.LoadPackageParam lpparam, String permission, Uri uriToBlock) {
        XC_MethodHook uriHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args[0] == uriToBlock) {
                    if (_isRestricted(permission, lpparam)) {
                        param.setResult(Constants.emptyCursor);
                    }
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

    private XC_MethodReplacement _createReplacementHook(String permission, XC_LoadPackage.LoadPackageParam lpparam, Object replacementValue) {
        return new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                if (_isRestricted(permission, lpparam)) {
                    return replacementValue;
                }
                return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
            }
        };
    }

    private XC_MethodHook _createAppListHook(XC_LoadPackage.LoadPackageParam lpparam, boolean packageInfo) {
        return new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(de.robv.android.xposed.XC_MethodHook.MethodHookParam param) throws Throwable {
                Context context = AndroidAppHelper.currentApplication();
                if (_isRestricted(Permissions.APP_LIST, lpparam) ) {
                    ArrayList<Object> apps = new ArrayList<>();
                    for (String packageName : m_pref.getStringSet(SharedPrefs.APP_LIST, new HashSet<>())) {
                        if (packageInfo) {
                            apps.add(context.getPackageManager().getPackageInfo(packageName, 0));
                        } else {
                            apps.add(context.getPackageManager().getApplicationInfo(packageName, 0));
                        }
                    }
                    param.setResult(apps);
                }
            }
        };
    };

    private XC_MethodHook _createLocationHook(XC_LoadPackage.LoadPackageParam lpparam, String sharePrefsKey) {
        return new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (_isRestricted(Permissions.LOCATION, lpparam) ) {
                    double location;
                    try {
                        location = Double.parseDouble(m_pref.getString(sharePrefsKey, "0.0"));
                    } catch (NumberFormatException _e) {
                        location = 0.0;
                    }
                    param.setResult(location);
                }
            }
        };
    }

    private boolean _isRestricted(String permission, XC_LoadPackage.LoadPackageParam lpparam) {
        if (!m_isRestricted.containsKey(permission)) {
            m_isRestricted.put(permission, DatabaseHelper.didUserRestrict(AndroidAppHelper.currentApplication(), permission, lpparam.packageName));
        }
        if (m_isRestricted.get(permission)) {
            _showNotification(permission, lpparam.appInfo);
            m_pref.reload();
            return true;
        }
        return false;
    }

    private void _showNotification(String Permission, ApplicationInfo appInfo) {
        // Create a notification and set the notification channel.
        Notification notification = new Notification.Builder(m_systemContext, NAME)
                .setContentTitle(NAME)
                .setContentText(Utils.getAppLabel(m_systemContext, appInfo)+ " tried to use: " + Permission + ".")
                .setChannelId(NAME)
                .build();
        // Issue the notification.
        m_notificationsManager.notify(1 , notification);
    }
}