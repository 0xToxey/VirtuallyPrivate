package com.virtuallyprivate;

import android.content.ClipData;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.location.Location;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaSyncEvent;

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
        if(m_dbManager.didUserRestrict(Permissions.MIC, lpparam.packageName)) {
            findAndHookMethod(AudioRecord.class,"startRecording", MediaSyncEvent.class, mediaHook);
            findAndHookMethod(AudioRecord.class,"startRecording", mediaHook);
            findAndHookMethod(MediaRecorder.class, "setAudioSource", int.class, mediaHook);
        }
    }
}