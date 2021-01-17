package com.virtuallyprivate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

class AppAdapter extends BaseExpandableListAdapter {
    private Context m_context;
    private DatabaseHelper m_dbHelper;
    private ArrayList<AppInfo> appArrayInfo;
    private final ArrayList<AppInfo> allApps; // all apps, don't change this!.
    private HashMap<AppInfo, ArrayList<String>> appList;

    public AppAdapter(Context context, HashMap<AppInfo, ArrayList<String>> apps, ArrayList<AppInfo> appArrayInfo, DatabaseHelper dbHelper){
        this.m_context = context;
        this.appList = apps;
        this.allApps = new ArrayList<>(appArrayInfo);
        this.appArrayInfo = appArrayInfo;
        this.m_dbHelper = dbHelper;
    }

    public void filter(String text) {
        text = text.toLowerCase(Locale.getDefault());
        ArrayList<String> availablePermissions = m_dbHelper.getAvailablePermissions();

        appList.clear();
        appArrayInfo.clear();

        for (AppInfo app : allApps) {
            if(text.length() == 0 ||  // if didn't search at all or did search for it.
                app.name.toLowerCase(Locale.getDefault()).startsWith(text)) {
                appList.put(app, availablePermissions);
                appArrayInfo.add(app);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return appArrayInfo.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return appList.get(appArrayInfo.get(i)).size();
    }

    @Override
    public AppInfo getGroup(int i) {
        return appArrayInfo.get(i);
    }

    @Override
    public String getChild(int i, int i1) {
        return appList.get(appArrayInfo.get(i)).get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        final AppInfo appInfo = getGroup(i);
        if(view == null)
            view = LayoutInflater.from(m_context).inflate(R.layout.app_view, null);

        final ImageView appImg = view.findViewById(R.id.appImg);
        final TextView appName = view.findViewById(R.id.appName);

        appImg.setImageDrawable(m_context.getPackageManager().getApplicationIcon(appInfo.info));
        appName.setText(appInfo.name);

        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        final AppInfo selectedApp = getGroup(i);
        final String permission = getChild(i, i1);

        if(view == null)
            view = LayoutInflater.from(m_context).inflate(R.layout.app_restrictions, null);

        final TextView permissionName = view.findViewById(R.id.permissionName);
        final CheckBox checkbox = view.findViewById(R.id.restrictionCheckbox);

        permissionName.setText(permission);
        checkbox.setChecked(m_dbHelper.didUserRestrict(permission, selectedApp.info.packageName));

        checkbox.setOnClickListener(v -> {
            Restriction userRestriction = new Restriction(selectedApp.info.packageName, m_dbHelper.getPermissionPrimaryKey(permission));
            if(checkbox.isChecked()) {
                m_dbHelper.addAppRestriction(userRestriction);
            } else {
                m_dbHelper.deleteAppRestriction(userRestriction);
            }

            Utils.forceStopApp(m_context, selectedApp.info);
        });

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
