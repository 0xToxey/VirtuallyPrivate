package com.example.firstmodule;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static android.app.PendingIntent.getActivity;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class MyModule implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // running in VirtualXposed?
        if (System.getProperty("vxp") == null)
            return;
        if (!lpparam.packageName.equals("com.example.writetexttest"))
            return;

        XposedBridge.log("Loaded app: " + lpparam.packageName);

        // can just set the result to null
//        findAndHookMethod("com.example.writetexttest.MainActivity", lpparam.classLoader, "readFile", String.class, new XC_MethodHook() {
//    		@Override
//    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//
//    		    // 2 options, 1 just set the result
//                // param.setResult("EMPTY");
//                // 2. throwing an exception
//                Context context = (Context) AndroidAppHelper.currentApplication();
//                Toast.makeText(context, "PRIVACY BREACH DETECTED", Toast.LENGTH_LONG).show();
//                param.setThrowable(new SecurityException("PRIVACY BREACH DETECTED"));
//            }
//    		@Override
//    		protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
//    		    String filename = (String) param.args[0];
//                XposedBridge.log("WE READ THE FILE: " + filename);
//    		}
//	    });

        // or entirely replace the method
        findAndHookMethod("com.example.writetexttest.MainActivity", lpparam.classLoader, "readFile", String.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                Context context = (Context) AndroidAppHelper.currentApplication();
                Toast.makeText(context, "PRIVACY BREACH DETECTED", Toast.LENGTH_LONG).show();
                return null;
            }
	    });
    }
}