package com.virtuallyprivate;

import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsAppChoose extends AppCompatActivity {

    SettingsAppAdapter m_appListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_app_choose);

        final ListView appChooseList = findViewById(R.id.settingsAppChoseList);
        m_appListAdapter = new SettingsAppAdapter(this, Utils.getUserInstallerApplications(this));
        appChooseList.setAdapter(m_appListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Utils.inflateOptionsMenu(menu, getMenuInflater(), false, m_appListAdapter);
        return super.onCreateOptionsMenu(menu);
    }
}
