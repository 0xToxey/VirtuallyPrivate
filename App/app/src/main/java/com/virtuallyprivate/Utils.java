package com.virtuallyprivate;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.widget.Toast;

import java.util.List;

public class Utils {
    public static void forceStopApp(Context context, ApplicationInfo appInfo) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        for(ActivityManager.RunningAppProcessInfo runningProcess : runningProcesses) {
            if(runningProcess.processName.equals((appInfo.packageName))) {
                android.os.Process.killProcess(runningProcess.pid);
            }
        }

        Toast.makeText(context,"Stopped: " +  Utils.getAppLabel(context, appInfo), Toast.LENGTH_SHORT).show();
    }

    public static String getAppLabel(Context context, ApplicationInfo app) {
        return (String) context.getPackageManager().getApplicationLabel(app);
    }
}
