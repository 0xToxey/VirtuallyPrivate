package com.virtuallyprivate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AppAdapter extends ArrayAdapter<AppInfo> {
    private Context m_context;
    private List<AppInfo> appList;
    private ArrayList<AppInfo> appArray;

    public AppAdapter(Context context, ArrayList<AppInfo> apps){
        super(context, 0, apps);
        this.m_context = context;
        this.appList = apps;
        this.appArray = new ArrayList<AppInfo>(apps);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        AppInfo appInfo = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.app_view, parent, false);
        }
        // Lookup view for data population
        ImageView appImg = (ImageView) convertView.findViewById(R.id.appImg);
        TextView appName = (TextView) convertView.findViewById(R.id.appName);

        // Populate the data into the template view using the data object
        appImg.setImageDrawable(getContext().getPackageManager().getApplicationIcon(appInfo.info));
        appName.setText(appInfo.name);

        // Set on click listener.
        LinearLayout view = (LinearLayout) convertView.findViewById(R.id.appView);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // selected item
                AppInfo selected = appInfo;
                Toast.makeText(m_context, selected.info.packageName, Toast.LENGTH_SHORT).show();
            }
        });

        // Return the completed view to render on screen
        return convertView;
    }

    public void filter (String text)
    {
        text = text.toLowerCase(Locale.getDefault());

        appList.clear();

        if (text.length() == 0)
            appList.addAll(appArray);

        else {
               for (AppInfo app : appArray) {
                   if (app.name.toLowerCase(Locale.getDefault()).startsWith(text))
                   {
                       appList.add(app);
                   }
               }
        }

        notifyDataSetChanged();
    }
}
