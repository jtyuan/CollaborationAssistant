package edu.pku.assistant.Group.member;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.pku.assistant.ContactInfoActivity;
import edu.pku.assistant.Group.util.adapter.MemberList.ContactAdapter;
import edu.pku.assistant.HomeActivity;
import edu.pku.assistant.R;
import edu.pku.assistant.Tool.Contact;
import edu.pku.assistant.Tool.Database;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;
import it.gmariotti.cardslib.library.view.CardView;


@SuppressWarnings("unchecked")
public class MemberFromContacts extends FragmentActivity {

    private Database database;
    private Cursor cursor;
    static ContentResolver resolver;

    private ListView ContactListView;

    public List<Contact> Contacts;
    private ContactAdapter ContactsAdapter;

    private int groupId;

    public ArrayList<Contact> newMembers;
    public ArrayList<Contact> members;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_contact);


        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            groupId = bundle.getInt("groupid");
            members = (ArrayList<Contact>) bundle.getSerializable("members");
        }


        ContactListView = (ListView) findViewById(R.id.contact_list);

        newMembers = new ArrayList<Contact>();

        database = new Database(this);
        setContacts();
    }

    private void setContacts() {

        Contacts = new ArrayList<Contact>();
        ContactsAdapter = new ContactAdapter(this, R.layout.contact_item, Contacts);

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
                Intent intent = new Intent(MemberFromContacts.this, ContactInfoActivity.class);
                intent.putExtra("contact", Contacts.get(i));
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("new_members", newMembers);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
                overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("new_members", newMembers);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
        overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
    }
}