package com.virtuallyprivate;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

class SettingsAppAdapter extends ArrayAdapter<ApplicationInfo> {
    private Context m_context;
    private SharedPreferences m_prefs;
    private Set<String> m_chosenApps;
    private ArrayList<ApplicationInfo> m_appArrayInfo;
    private final ArrayList<ApplicationInfo> m_allApps; // all apps, don't change this!.

    public SettingsAppAdapter(@NonNull Context context, ArrayList<ApplicationInfo> appArrayInfo) {
        super(context, 0, appArrayInfo);
        this.m_context = context;
        this.m_prefs = m_context.getSharedPreferences(VirtuallyPrivate.NAME, 0);;
        this.m_allApps = new ArrayList<>(appArrayInfo);
        this.m_appArrayInfo = appArrayInfo;
        this.m_chosenApps = m_prefs.getStringSet(SharedPrefs.APP_LIST, new HashSet<>());
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View listItem, @NonNull ViewGroup parent) {
        if(listItem == null)
            listItem = LayoutInflater.from(m_context).inflate(R.layout.app_view,parent,false);

        final ApplicationInfo currApp = m_appArrayInfo.get(position);

        // Lookup view for data population
        final ImageView appImg = listItem.findViewById(R.id.appImg);
        final TextView appName = listItem.findViewById(R.id.appName);
        final CheckBox appChoose = listItem.findViewById(R.id.appChooseCheckbox);

        appImg.setImageDrawable(m_context.getPackageManager().getApplicationIcon(currApp));
        appName.setText(Utils.getAppLabel(m_context, currApp));
        appChoose.setVisibility(View.VISIBLE);

        appChoose.setOnCheckedChangeListener(_getChooseAppListener(currApp.packageName));

        // showing the state of the checkbox
        if (m_chosenApps.contains(currApp.packageName)) {
            appChoose.setChecked(true);
        } else {
            appChoose.setChecked(false);
        }

        return listItem;
    }

    public void filter(String text) {
        text = text.toLowerCase();

        m_appArrayInfo.clear();
        for (ApplicationInfo app : m_allApps) {
            final String appName = Utils.getAppLabel(m_context, app);
            if(text.isEmpty() ||  // if didn't search at all or did search for it.
                appName.toLowerCase().startsWith(text)) {
                m_appArrayInfo.add(app);
            }
        }
        notifyDataSetChanged();
    }

    private CompoundButton.OnCheckedChangeListener _getChooseAppListener(String packageName) {
        return (compoundButton, isChecked) -> {
            if (isChecked) {
                m_chosenApps.add(packageName);
            } else {
                m_chosenApps.remove(packageName);
            }

            m_prefs.edit().remove(SharedPrefs.APP_LIST).apply();
            m_prefs.edit().putStringSet(SharedPrefs.APP_LIST, m_chosenApps).apply();
        };
    }
}
