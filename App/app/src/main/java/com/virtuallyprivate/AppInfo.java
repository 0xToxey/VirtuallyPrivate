package com.virtuallyprivate;

import android.content.pm.ApplicationInfo;

public class AppInfo {
    public  String name;
    public ApplicationInfo info;

    public AppInfo(String name, ApplicationInfo info)
    {
        this.name = name;
        this.info = info;
    }
}
