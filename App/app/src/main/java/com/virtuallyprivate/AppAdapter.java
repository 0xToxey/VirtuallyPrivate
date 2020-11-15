package com.virtuallyprivate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AppAdapter extends BaseExpandableListAdapter {
    private Context m_context;
    private ArrayList<AppInfo> appArrayInfo;
    private ArrayList<AppInfo> allApps; // all apps, don't change this!.
    private Map<AppInfo, ArrayList<String>> appList;
    private DatabaseManager dbManager;

    public AppAdapter(Context context, Map<AppInfo, ArrayList<String>> apps, ArrayList<AppInfo> appArrayInfo, DatabaseManager dbManager){
        this.m_context = context;
        this.appList = apps;
        this.allApps = new ArrayList<AppInfo>(appArrayInfo);
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
        AppInfo appInfo = (AppInfo) getGroup(i);
        // Check if an existing view is being reused, otherwise inflate the view
        if(view == null) {
            LayoutInflater inflater = (LayoutInflater) m_context.getSystemService(m_context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.app_view, null);
        }

        // Lookup view for data population
        ImageView appImg = view.findViewById(R.id.appImg);
        TextView appName = (TextView) view.findViewById(R.id.appName);

        // Populate the data into the template view using the data object
        appImg.setImageDrawable(m_context.getPackageManager().getApplicationIcon(appInfo.info));
        appName.setText(appInfo.name);

        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        String permission = getChild(i, i1);

        // Check if an existing view is being reused, otherwise inflate the view
        if(view == null) {
            LayoutInflater inflater = (LayoutInflater) m_context.getSystemService(m_context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.app_restrictions, null);
        }

        // Lookup view for data population
        TextView permissionName = view.findViewById(R.id.permissionName);

        // Populate the data into the template view using the data object
        permissionName.setText(permission);

        // Set on click listener.
        CheckBox checkbox = view.findViewById(R.id.restrictionCheckbox);
        permissionName.setOnClickListener(v -> {
            // selected item
            AppInfo selected = getGroup(i);
            Restriction userRestriction = new Restriction(selected.info.packageName, dbManager.getPermissionPrimaryKey(permission));
            if(!checkbox.isChecked()) { // by the time the onClick listener is called, the check is reversed.
                dbManager.addAppRestriction(userRestriction);
            } else {
                dbManager.deleteAppRestriction(userRestriction);
            }

            checkbox.setChecked(!checkbox.isChecked());
            Toast.makeText(
                m_context,
                selected.info.packageName + " blocked permission: " + permission,
                Toast.LENGTH_SHORT).show();
        });

        // Return the completed view to render on screen
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
