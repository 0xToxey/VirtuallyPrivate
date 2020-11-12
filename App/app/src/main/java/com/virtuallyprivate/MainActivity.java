package com.virtuallyprivate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.icu.util.ULocale;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ListView listview;
    AppAdapter appListAdapter;

    protected void createAvailablePermissions() {
        // if it is the first time the app ran, it saves the available permissions in the db.
        final String PREFS_NAME = "VirtuallyPrivate";
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        if (settings.getBoolean("fresh_install", true)) {
            DatabaseManager dbManager = new DatabaseManager(MainActivity.this);
            // add permissions like this
            dbManager.addPermission(new Permission( "permission_1"));
            // record the fact that the app has been started at least once
            settings.edit().putBoolean("fresh_install", false).commit();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createAvailablePermissions();
        DatabaseManager dbManager = new DatabaseManager(MainActivity.this);

        Permission p = new Permission("permission_1");

        listview = (ListView) findViewById(R.id.listview);
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        LoadApps(dbManager);
    }

    /*Load the applications on user's phone*/
    private void LoadApps(DatabaseManager dbManager) {
        List<ApplicationInfo> pkgAppsList = getPackageManager().getInstalledApplications(0);

        final ArrayList<AppInfo> list = new ArrayList<AppInfo>();

        for (ApplicationInfo app: pkgAppsList) {
            // Check that it is only user-installed app.
            if (!((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0))
            {
                AppInfo info = new AppInfo((String) getPackageManager().getApplicationLabel(app), app);
                list.add(info);
            }
        }

        appListAdapter = new AppAdapter(this, list, dbManager);
        listview.setAdapter(appListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search_bar, menu);
        MenuItem menuItem = menu.findItem(R.id.search_icon);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search..");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    appListAdapter.filter("");
                }
                else {
                    appListAdapter.filter(newText);
                }

                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
}