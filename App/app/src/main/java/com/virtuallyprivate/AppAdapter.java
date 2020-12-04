package com.virtuallyprivate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

class AppAdapter extends BaseExpandableListAdapter {
    private Context m_context;
    private ArrayList<AppInfo> appArrayInfo;
    private final ArrayList<AppInfo> allApps; // all apps, don't change this!.
    private HashMap<AppInfo, ArrayList<String>> appList;
    private DatabaseManager dbManager;

    public AppAdapter(Context context, HashMap<AppInfo, ArrayList<String>> apps, ArrayList<AppInfo> appArrayInfo, DatabaseManager dbManager){
        this.m_context = context;
        this.appList = apps;
        this.allApps = new ArrayList<>(appArrayInfo);
        this.appArrayInfo = appArrayInfo;
        this.dbManager = dbManager;
    }

    public void filter(String text) {
        text = text.toLowerCase(Locale.getDefault());
        ArrayList<String> availablePermissions = dbManager.getAvailablePermissions();

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
        AppInfo appInfo = getGroup(i);
        // Check if an existing view is being reused, otherwise inflate the view
        if(view == null) {
            LayoutInflater inflater = (LayoutInflater) m_context.getSystemService(m_context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.app_view, null);
        }

        // Lookup view for data population
        ImageView appImg = view.findViewById(R.id.appImg);
        TextView appName = view.findViewById(R.id.appName);

        // Populate the data into the template view using the data object
        appImg.setImageDrawable(m_context.getPackageManager().getApplicationIcon(appInfo.info));
        appName.setText(appInfo.name);

        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        AppInfo selectedApp = getGroup(i);
        String permission = getChild(i, i1);

        // Check if an existing view is being reused, otherwise inflate the view
        if(view == null) {
            LayoutInflater inflater = (LayoutInflater) m_context.getSystemService(m_context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.app_restrictions, null);
        }

        TextView permissionName = view.findViewById(R.id.permissionName);
        permissionName.setText(permission);
        CheckBox checkbox = view.findViewById(R.id.restrictionCheckbox);
        checkbox.setChecked(this.dbManager.didUserRestrict(permission, selectedApp.info.packageName));

        // Set on click listener.
        checkbox.setOnClickListener(v -> {
            // selected item
            Restriction userRestriction = new Restriction(selectedApp.info.packageName, dbManager.getPermissionPrimaryKey(permission));
            if(checkbox.isChecked()) { // by the time the onClick listener is called, the check is reversed.
                dbManager.addAppRestriction(userRestriction);
            } else {
                dbManager.deleteAppRestriction(userRestriction);
            }

            Toast.makeText(
                m_context,
                    selectedApp.info.packageName + " blocked permission: " + permission,
                Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
