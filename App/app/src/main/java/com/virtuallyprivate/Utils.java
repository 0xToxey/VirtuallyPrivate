package com.virtuallyprivate;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static void forceStopApp(Context context, ApplicationInfo appInfo) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningProcess : runningProcesses) {
            if(runningProcess.processName.equals((appInfo.packageName))) {
                android.os.Process.killProcess(runningProcess.pid);
            }
        }

        Toast.makeText(context,"Stopped: " +  Utils.getAppLabel(context, appInfo), Toast.LENGTH_SHORT).show();
    }

    public static String getAppLabel(Context context, ApplicationInfo app) {
        return (String) context.getPackageManager().getApplicationLabel(app);
    }

    public static ArrayList<ApplicationInfo> getUserInstallerApplications(Context context) {
        final ArrayList<ApplicationInfo> appArrayInfo = new ArrayList<>();
        for (ApplicationInfo app: context.getPackageManager().getInstalledApplications(0)) {
            // Check that it is only user-installed app.
            if (!app.packageName.equals(BuildConfig.APPLICATION_ID) && (app.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
                appArrayInfo.add(app);
        }
        return appArrayInfo;
    }

    public static void inflateOptionsMenu(Menu menu, MenuInflater menuInflater, boolean showSettingsButton, Object appListAdapter) {
        menuInflater.inflate(R.menu.search_bar, menu);
        menu.findItem(R.id.settings_button).setVisible(showSettingsButton);

        MenuItem menuItem = menu.findItem(R.id.search_icon);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search..");
        searchView.setIconifiedByDefault(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                try {
                    appListAdapter.getClass().getMethod("filter", String.class).invoke(appListAdapter, newText);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
    }

    public static String getProcessNameForPid(Context context, int callingPid) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> infos = am.getRunningAppProcesses();
        if (infos != null && infos.size() > 0) {
            for(ActivityManager.RunningAppProcessInfo info : infos) {
                if(info.pid == callingPid) {
                   return info.processName;
                }
            }
        }
        return "";
    }
}
