package com.virtuallyprivate;

import android.app.Activity;
import android.app.AndroidAppHelper;
import android.content.ClipData;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
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
        // Clipboard
        if (m_dbManager.didUserRestrict(Permissions.CLIPBOARD, lpparam.packageName)) {
            findAndHookMethod("android.content.ClipData", lpparam.classLoader, "getItemAt", int.class, new XC_MethodHook() {
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
    }
}