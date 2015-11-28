package edu.pku.assistant.Fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import edu.pku.assistant.ContactInfoActivity;
import edu.pku.assistant.HomeActivity;
import edu.pku.assistant.R;
import edu.pku.assistant.Tool.Contact;
import edu.pku.assistant.Tool.ContactAdapter;
import edu.pku.assistant.Tool.Database;


public class ContactFragment extends Fragment {

    private static final String FRAGMENT_TITLE_CN = "联系人";
    private static final String FRAGMENT_TITLE_EN = "Contacts";

    private Database database;
    private Cursor cursor;
    static ContentResolver resolver;

    private ListView ContactListView;

    private List<Contact> Contacts;
    private ContactAdapter ContactsAdapter;

    public static ContactFragment newInstance() {
        ContactFragment fragment = new ContactFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    public ContactFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contact, container, false);
        return rootView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ContactListView = (ListView) getActivity().findViewById(R.id.contact_list);

        database = new Database(getActivity());
        setContacts();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getResources().getConfiguration().locale.getCountry().equals("CN")) {
            ((HomeActivity) activity).onSectionAttached(FRAGMENT_TITLE_CN);
        } else {
            ((HomeActivity) activity).onSectionAttached(FRAGMENT_TITLE_EN);
        }
    }

    private void setContacts() {

        Contacts = new ArrayList<Contact>();
        ContactsAdapter = new ContactAdapter(getActivity(), R.layout.contact_item, Contacts);

        database.Open();
        cursor = database.getContacts();
        if (cursor.moveToFirst()) {
            for (; !cursor.isAfterLast(); cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                int userid = cursor.getInt(2);
                String username = cursor.getString(3);
                String phone = cursor.getString(4);
                Contact contact = new Contact(id, name, userid, username, phone);
                Contacts.add(contact);
            }
        }
        database.Close();
        ContactListView.setAdapter(ContactsAdapter);
        ContactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), ContactInfoActivity.class);
                intent.putExtra("contact", Contacts.get(i));
                startActivity(intent);
            }
        });
    }
}