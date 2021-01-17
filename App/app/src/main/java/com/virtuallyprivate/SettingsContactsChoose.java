package com.virtuallyprivate;

import androidx.appcompat.app.AppCompatActivity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.widget.ListView;
import java.util.ArrayList;

public class SettingsContactsChoose extends AppCompatActivity {

    SettingsContactsAdapter m_contactListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_contacts_choose);

        final ListView appChooseList = findViewById(R.id.ContactsChoseList);
        m_contactListAdapter = new SettingsContactsAdapter(this, getAllContacts());
        appChooseList.setAdapter(m_contactListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Utils.inflateOptionsMenu(menu, getMenuInflater(), false, m_contactListAdapter);
        return super.onCreateOptionsMenu(menu);
    }

    private ArrayList<Contact> getAllContacts() {
        ArrayList<Contact> contactsList = new ArrayList<>();

        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        while (cur != null && cur.moveToNext()) {
            String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
            String name = cur.getString(cur.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY));

            Contact con = new Contact(name, id);
            contactsList.add(con);
        }
        if(cur != null){
            cur.close();
        }

        return contactsList;
    }
}