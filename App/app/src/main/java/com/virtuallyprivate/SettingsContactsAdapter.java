package com.virtuallyprivate;

import android.content.Context;
import android.content.SharedPreferences;
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

public class SettingsContactsAdapter extends ArrayAdapter<Contact> {
    private Context m_context;
    private SharedPreferences m_prefs;
    private Set<String> m_chosenContacts_ids;
    private ArrayList<Contact> m_contactsArray; // Contacts that are on screen
    private final ArrayList<Contact> m_allContacts; // all contacts, don't change this!.

    public SettingsContactsAdapter(@NonNull Context context, ArrayList<Contact> contactsArray) {
        super(context, 0, contactsArray);
        this.m_context = context;
        this.m_contactsArray = contactsArray;
        this.m_allContacts = contactsArray;
        this.m_prefs = m_context.getSharedPreferences(VirtuallyPrivate.NAME, 0);
        this.m_chosenContacts_ids = m_prefs.getStringSet(SharedPrefs.CONTACTS, new HashSet<>());
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View listItem, @NonNull ViewGroup parent) {
        if(listItem == null)
            listItem = LayoutInflater.from(m_context).inflate(R.layout.app_view, parent, false);

        final Contact currContact = m_contactsArray.get(position);

        // Lookup view for data population
        final ImageView appImg = listItem.findViewById(R.id.appImg);
        final TextView appName = listItem.findViewById(R.id.appName);
        final CheckBox appChoose = listItem.findViewById(R.id.appChooseCheckbox);

        appImg.setImageDrawable(null);
        appName.setText(currContact.name);
        appChoose.setVisibility(View.VISIBLE);

        appChoose.setOnCheckedChangeListener(_getChooseContactListener(currContact));

        // showing the state of the checkbox
        appChoose.setChecked(m_chosenContacts_ids.contains(currContact.id));
        return listItem;
    }

    public void filter(String text) {
        text = text.toLowerCase();

        m_contactsArray.clear();
        for (Contact contact : m_allContacts) {
            if(text.isEmpty() ||  // if didn't search at all or did search for it.
                    contact.name.toLowerCase().startsWith(text)) {
                m_allContacts.add(contact);
            }
        }
        notifyDataSetChanged();
    }

    private CompoundButton.OnCheckedChangeListener _getChooseContactListener(Contact contact) {
        return (compoundButton, isChecked) -> {
            if (isChecked) {
                m_chosenContacts_ids.add(contact.id);
            }
            else {
                m_chosenContacts_ids.remove(contact.id);
            }

            m_prefs.edit().remove(SharedPrefs.CONTACTS).apply();
            m_prefs.edit().putStringSet(SharedPrefs.CONTACTS, m_chosenContacts_ids).apply();
        };
    }

}
