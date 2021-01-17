package com.virtuallyprivate;

import android.app.AndroidAppHelper;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ContentResolver;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
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
        if(lpparam.packageName.equals(BuildConfig.APPLICATION_ID)) {
            return;
        }

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
        this._hookContentResolverQuery(lpparam, Permissions.CONTACTS_LIST, ContactsContract.Data.CONTENT_URI);
        this._hookContentResolverQuery(lpparam, Permissions.CONTACTS_LIST, ContactsContract.DeletedContacts.CONTENT_URI);
        this._hookContentResolverQuery(lpparam, Permissions.CONTACTS_LIST, ContactsContract.CommonDataKinds.Contactables.CONTENT_URI);
        this._hookContentResolverQuery(lpparam, Permissions.CONTACTS_LIST, ContactsContract.RawContacts.CONTENT_URI);
        this._hookContentResolverQuery(lpparam, Permissions.CONTACTS_LIST, ContactsContract.Contacts.CONTENT_URI);

        // call log
        this._hookContentResolverQuery(lpparam, Permissions.CALL_LOG, CallLog.Calls.CONTENT_URI);
    }

    private void _hookContentResolverQuery(XC_LoadPackage.LoadPackageParam lpparam, String permission, Uri uriToBlock) {
        XC_MethodHook contentResolverHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param)  {
                final Uri hookedUri = (Uri)param.args[0];
                if (hookedUri.getAuthority().equals(uriToBlock.getAuthority()) && hookedUri.getPath().equals(uriToBlock.getPath())) {
                    if (!_isRestricted(permission, lpparam)) {
                        return;
                    }
                    if (Permissions.CONTACTS_LIST.equals(permission)) {
                        _contactsBeforeHookedMethod(param, hookedUri, m_pref.getStringSet(SharedPrefs.CONTACTS, new HashSet<>()));
                    } else {
                        param.setResult(Constants.emptyCursor);
                    }
                }
            }
        };
        findAndHookMethod("android.content.ContentResolver", lpparam.classLoader, "query", Uri.class,
                String[].class, String.class, String[].class, String.class, contentResolverHook);
        findAndHookMethod("android.content.ContentResolver", lpparam.classLoader, "query", Uri.class,
                String[].class, Bundle.class, CancellationSignal.class, contentResolverHook);
        findAndHookMethod("android.content.ContentResolver", lpparam.classLoader, "query", Uri.class,
                String[].class, String.class, String[].class, String.class, CancellationSignal.class, contentResolverHook);
    }

    private void _contactsBeforeHookedMethod(XC_MethodHook.MethodHookParam param, Uri hookedUri, Set<String> contactIdsSet) {
        if(contactIdsSet.isEmpty()) {
            param.setResult(Constants.emptyCursor);
            return;
        }

        // In case of ContactsContract.Data.CONTENT_URI or ContactsContract.RawContacts.CONTENT_URI contactIdColumnName should be 'contact_id'
        // else (ContactsContract.Contacts.CONTENT_URI), should be '_id'
        final String contactIdColumnName = hookedUri.getPath().equals(ContactsContract.Contacts.CONTENT_URI.getPath()) ?
                ContactsContract.Contacts._ID : ContactsContract.Data.CONTACT_ID;

        // adding contact id projection
        ArrayList<String> projection = new ArrayList<>(Arrays.asList((String[]) param.args[1]));
        if(!projection.contains(contactIdColumnName)) {
            projection.add(contactIdColumnName);
        }

        // params are uri hook(0), projection(1), selection(2), selection args(3)
        // or uri hook(0), projection(1), bundle(2)
        String originalSelection;
        String contactIdsSelection = contactIdColumnName + " IN (" + String.join(",", contactIdsSet) + ")";
        String[] selectionArgs;
        if (param.args[2] instanceof Bundle) {
            Bundle arguments = ((Bundle) param.args[2]);
            originalSelection = arguments.getString(ContentResolver.QUERY_ARG_SQL_SELECTION, "");
            selectionArgs = arguments.getStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS);
        } else {
            originalSelection = (String) param.args[2];
            selectionArgs = (String[]) param.args[3];
        }

        if(originalSelection != null) {
            //  if already selecting only the wanted contact ids, returning to the original function
            if (originalSelection.startsWith(contactIdsSelection)) {
                return;
            }
            contactIdsSelection += " AND (" + originalSelection + ")";
        }
        Object[] queryParams = new Object[]{
                hookedUri, projection.toArray(new String[0]),
                contactIdsSelection, selectionArgs, null
        };
        final Method queryMethod = XposedHelpers.findMethodExact(ContentResolver.class, "query", Uri.class,
                String[].class, String.class, String[].class, String.class);
        try {
            param.setResult(XposedBridge.invokeOriginalMethod(
                    queryMethod, AndroidAppHelper.currentApplication().getContentResolver(), queryParams));
        } catch (IllegalAccessException | InvocationTargetException e) {
            param.setResult(Constants.emptyCursor);
        }
    }

     /*
     * function returns a hook used for replacement, it sets the param result
     * to given replacementValue if the permission is restricted
     * */
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

    /* function returns a hook used for app list, it sets the param result
     * to the shared prefs app list
     * */
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

    /* function returns a hook used for location, it sets the param result to 0.0 if
     * no sharedPrefs value is set
     * */
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

    /* function checks if the permission is restricted, if so it shows notification and reloads shared prefs */
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

    /* function shows a notification for a used permission */
    private void _showNotification(String Permission, ApplicationInfo appInfo) {
        // Create a notification and set the notification channel.
        Notification notification = new Notification.Builder(m_systemContext, NAME)
                .setContentTitle(NAME)
                .setContentText(Utils.getAppLabel(m_systemContext, appInfo) + " tried to use: " + Permission + ".")
                .setChannelId(NAME)
                .setOnlyAlertOnce(true)
                .build();
        // Issue the notification.
        m_notificationsManager.notify(1, notification);
    }
}