package com.virtuallyprivate;

import android.app.AndroidAppHelper;
import android.content.ClipData;
import android.content.Context;
import android.widget.Toast;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
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
        this._hookPermissions(lpparam);
    }

    private void _hookPermissions(XC_LoadPackage.LoadPackageParam lpparam) {
        if( m_dbManager == null) {
            Context systemContext = (Context) XposedHelpers.callMethod( XposedHelpers.callStaticMethod( XposedHelpers.findClass("android.app.ActivityThread", lpparam.classLoader), "currentActivityThread"), "getSystemContext" );
            m_dbManager = new DatabaseManager(systemContext);
        }

        // Clipboard
        if (this.m_dbManager.didUserRestrict("Clipboard", lpparam.packageName)) {
            findAndHookMethod("android.content.ClipData", lpparam.classLoader, "getItemAt", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
                    param.setResult(new ClipData.Item(""));
                }
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) {
                    XposedBridge.log("Tried to read the clipboard");
                }
            });
        }
    }
}