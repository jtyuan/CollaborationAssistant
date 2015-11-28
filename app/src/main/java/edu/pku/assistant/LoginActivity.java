package edu.pku.assistant;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import edu.pku.assistant.Group.util.Group;
import edu.pku.assistant.Group.util.TimeSeg;
import edu.pku.assistant.Tool.Constants;
import edu.pku.assistant.Tool.Contact;
import edu.pku.assistant.Tool.CustomerHttpClient;
import edu.pku.assistant.Tool.Database;


public class LoginActivity extends Activity {
    private static final String SHOW_INTRO = "show_intro";

    private Button loginButton;
    private Button signupButton;
    private TextView phoneTextView;
    private TextView passwordTextView;
    private String phone;
    private String password;
    private ArrayList<Contact> contacts;

    SharedPreferences mConfig;
    SharedPreferences.Editor mConfigEditor;
    Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        phoneTextView = (TextView)this.findViewById(R.id.login_phone);
        passwordTextView = (TextView)this.findViewById(R.id.login_password);
        loginButton = (Button)this.findViewById(R.id.login_login);
        signupButton = (Button)this.findViewById(R.id.login_register);

        contacts = new ArrayList<Contact>();
        mConfig = getSharedPreferences("account", Context.MODE_PRIVATE);
        phone = mConfig.getString("phone", null);
        password = mConfig.getString("password", null);
        phoneTextView.setText(phone);
        passwordTextView.setText(password);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phone = phoneTextView.getText().toString();
                password = passwordTextView.getText().toString();
                if (phone.isEmpty())
                    Toast.makeText(LoginActivity.this, R.string.login_empty_phone, Toast.LENGTH_SHORT).show();
                else if(password.isEmpty())
                    Toast.makeText(LoginActivity.this, R.string.login_empty_password, Toast.LENGTH_SHORT).show();
                db = new Database(getApplicationContext());
                db.Open();
                db.Clear();
                setPhoneContacts();
                new LoginTask(phone, password).execute();
            }
        });
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    void setPhoneContacts() {
        final String[] PHONES_PROJECTION = new String[]{ ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.PHOTO_ID};
        ContentResolver resolver = getContentResolver();
        Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PHONES_PROJECTION, null, null, ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY);
        if (phoneCursor != null) {
            while (phoneCursor.moveToNext()) {

                String phoneNumber = (phoneCursor.getString(1)).replaceAll(" ", "");
                if (TextUtils.isEmpty(phoneNumber))
                    continue;

                String name = phoneCursor.getString(0);
                Long contactid = phoneCursor.getLong(2);
                Long photoid = phoneCursor.getLong(3);

                Contact contact = new Contact(name, phoneNumber);
                if (photoid > 0) {
                    Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactid);
                    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(resolver, uri);
                    contact.setThumbnail(BitmapFactory.decodeStream(input));
                }else {
                    contact.setThumbnail(null);
                }
                contacts.add(contact);
                //db.InsertContacts(-1, contact.getName(), contact.getId(), contact.getUserName(), contact.getUserId(), contact.getPhone());
            }
            phoneCursor.close();
        }
        Uri uri = Uri.parse("content://icc/adn");
        phoneCursor = resolver.query(uri, PHONES_PROJECTION, null, null, null);
        if (phoneCursor != null) {
            while (phoneCursor.moveToNext()) {

                String phoneNumber = phoneCursor.getString(1);
                if (TextUtils.isEmpty(phoneNumber))
                    continue;
                String name = phoneCursor.getString(0);
                Long contactid = phoneCursor.getLong(2);
                Long photoid = phoneCursor.getLong(3);

                Contact contact = new Contact(name, phoneNumber);
                if (photoid > 0) {
                    uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactid);
                    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(resolver, uri);
                    contact.setThumbnail(BitmapFactory.decodeStream(input));
                }else {
                    contact.setThumbnail(null);
                }
                contacts.add(contact);
                //db.InsertContacts(-1, contact.getName(), contact.getId(), contact.getUserName(), contact.getUserId(), contact.getPhone());
            }
            phoneCursor.close();
        }
    }

    public class LoginTask extends AsyncTask<Void, Void, Boolean> {
        private String mPhone;
        private String mPassword;
        private int userId;
        private String msg;

        LoginTask(String phone, String password) {
            mPhone = phone;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... param) {
            Boolean retValue = false;
            String respond;

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("phone", mPhone));
            params.add(new BasicNameValuePair("password", mPassword));
            try {
                respond = CustomerHttpClient.post(Constants.base_url + "contact/index.php/contact/login", params);
            } catch (RuntimeException e) {
                respond = null;
            }
            if (respond == null) {
                retValue = false;
            } else {
                try {
                    JSONObject jsonInfo = new JSONObject(respond);
                    int result = jsonInfo.getInt("result");
                    msg = jsonInfo.getString("msg");
                    switch (result) {
                        case 0:
                            retValue = false;
                            break;
                        case 1:
                            userId = jsonInfo.getInt("userid");
                            String userName = jsonInfo.getString("username");
                            mConfigEditor = mConfig.edit();
                            mConfigEditor.putString("phone", mPhone);
                            mConfigEditor.putString("password", mPassword);
                            mConfigEditor.putInt("userid", userId);
                            mConfigEditor.putString("username", userName);
                            mConfigEditor.commit();
                            retValue = true;
                            break;
                        default:
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return retValue;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            Intent intent;
            if (success)
            {
                Toast.makeText(LoginActivity.this, LoginActivity.this.getResources().getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                //setPhoneContacts();
                new CompareContactTask(userId, contacts).execute();

            }
            else
            {
                Toast.makeText(LoginActivity.this, R.string.login_fail, Toast.LENGTH_SHORT).show();
//                Log.d("login-", msg);
            }
        }
    }

    public class CompareContactTask extends  AsyncTask<Void, Void, Integer> {
        private ArrayList<Contact> contacts;
        private int userId;

        CompareContactTask(int userId, ArrayList<Contact> contacts) {
            this.userId = userId;
            this.contacts = contacts;
        }

        @Override
        protected  Integer doInBackground(Void... param) {
            Integer retValue = -1;
            String respond;
            Log.d("LoginActivity", "reached compare");
            StringBuilder phonelist = new StringBuilder(1024);
            for (int i = 0; i < contacts.size(); i++) {
                phonelist.append(contacts.get(i).getPhone()).append(",");
            }
            if (phonelist.length() > 0) {
                phonelist.deleteCharAt(phonelist.length() - 1);
            }
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            if (phonelist.length() > 0) {
                params.add(new BasicNameValuePair("phonelist", phonelist.toString()));
            }

            try {
                respond = CustomerHttpClient.post(Constants.base_url + "contact/index.php/contact/comparecontact", params);
            } catch (RuntimeException e) {
                respond = null;
            }
            if (respond == null) {
                retValue = -1;
            } else {
                try {
                    JSONObject jsonInfo = new JSONObject(respond);
                    int result = jsonInfo.getInt("result");
                    String msg = jsonInfo.getString("msg");
                    if (result == 0) retValue = 1; // retValue = 0; in case for no local contacts
                    else {
                        retValue = result;
                        JSONArray contactsinfo = jsonInfo.getJSONArray("contactlist");
                        int count = contactsinfo.length();
                        for (int i = 0; i < count; i++) {
                            contacts.get(i).setUserId(contactsinfo.getJSONObject(i).getInt("userid"));
                            contacts.get(i).setUserName(contactsinfo.getJSONObject(i).getString("username"));
                            contacts.get(i).setId(contactsinfo.getJSONObject(i).getInt("renrenid"));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return retValue;
        }

        @Override
        protected void onPostExecute(Integer success) {
            if (success == 1) {
                for (int i = 0; i < contacts.size(); i++){
                    Contact contact = contacts.get(i);
                    db.InsertContacts(-1, contact.getName(), contact.getId(), contact.getUserName(), contact.getUserId(), contact.getPhone());
                }
                new FetchGroupTask(userId).execute();
            }
        }
    }

    public class FetchGroupTask extends AsyncTask<Void, Void, Integer> {
        private int userId;
        private String msg;

        FetchGroupTask(int userId) {
            this.userId = userId;
        }

        @Override
        protected Integer doInBackground(Void... param) {
            Integer retValue = -1;
            String respond;

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("userid", String.valueOf(userId)));
            try {
                respond = CustomerHttpClient.post(Constants.base_url + "contact/index.php/group/listgroup", params);
            } catch (RuntimeException e) {
                respond = null;
            }
            if (respond == null) {
                retValue = -1;
            } else {
                try {
                    JSONObject jsonInfo = new JSONObject(respond);
                    int result = jsonInfo.getInt("result");
                    msg = jsonInfo.getString("msg");
                    if (result == 0) retValue = 0;
                    else {
                        retValue = result;
                        JSONArray groupsinfo = jsonInfo.getJSONArray("groupsinfo");
                        int count = groupsinfo.length();
                        Database db = new Database(getApplicationContext());
                        db.Open();
                        for (int i = 0; i < count; ++i) {
                            Calendar createDate = Calendar.getInstance();
                            createDate.setTime(new Date(
                                    Long.parseLong(String.valueOf(groupsinfo.getJSONObject(i).getInt("createtime")) + "000")));
                            String[] title_raw = groupsinfo.getJSONObject(i).getString("name").split(",");
                            Group group = new Group(
                                    groupsinfo.getJSONObject(i).getInt("groupid"),
                                    title_raw[0],
                                    groupsinfo.getJSONObject(i).getInt("createuser"),
                                    createDate);

                            String beginTime_raw = groupsinfo.getJSONObject(i).getString("begintime");
                            String endTime_raw = groupsinfo.getJSONObject(i).getString("endtime");
                            String[] beginTimes = null, endTimes = null;
                            if (beginTime_raw != null)
                                beginTimes = beginTime_raw.split(",");
                            if (endTime_raw != null)
                                endTimes = endTime_raw.split(",");

                            TimeSeg timeSeg;

                            if (beginTimes != null) {
                                for (int j = 0; j < beginTimes.length; ++j) {
                                    String beginTime = beginTimes[j];
                                    String endTime = endTimes[j];
                                    if (!beginTime.equals("null") && !endTime.equals("null")) {
                                        Calendar beginDate = Calendar.getInstance(), endDate = Calendar.getInstance();
                                        beginDate.setTime(new Date(
                                                Long.parseLong(beginTime + "000")));
                                        endDate.setTime(new Date(
                                                Long.parseLong(endTime + "000")));
                                        String desc = getResources().getString(R.string.ts_description);
                                        if (j+1 < title_raw.length)
                                            desc = title_raw[j+1];
                                        timeSeg = new TimeSeg(beginDate, endDate, desc);
                                        Log.d("fetch_group", "timeseg: " + timeSeg.toString());
                                        if (group.rendezvous != null)
                                            group.rendezvous.add(timeSeg);
                                    }
                                }
                            }

                            JSONArray memberlist = groupsinfo.getJSONObject(i).getJSONArray("grouprelation");
                            int memberNum = memberlist.length();
                            db.InsertGroup(group.groupId, group.formatGroupName(), String.valueOf(group.creatorId),
                                    String.valueOf(group.createDate.getTimeInMillis() / 1000));
                            if (group.rendezvous.size() > 0) {
                                db.UpdateGroupTime(group.groupId,
                                        groupsinfo.getJSONObject(i).getString("begintime"),
                                        groupsinfo.getJSONObject(i).getString("endtime"));
                            }
                            for (int j = 1; j < memberNum; ++j) {
                                Contact member = new Contact(memberlist.getJSONObject(j).getInt("id"),
                                        memberlist.getJSONObject(j).getString("name"));
                                Log.d("listgroup_member", member.getName()+","+member.getId());
                                db.InsertContacts(group.groupId, member.getName(),member.getId(), member.getUserName(), member.getUserId(), member.getPhone());
                            }
                        }
                        db.Close();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return retValue;
        }

        @Override
        protected void onPostExecute(Integer success) {
            switch(success) {
                case -1:
                    Toast.makeText(getApplicationContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Boolean flag = getSharedPreferences(SHOW_INTRO, MODE_PRIVATE).getBoolean("flag", true);
                    Intent intent;
                    if (flag) {
                        intent = new Intent(LoginActivity.this, IntroActivity.class);
                        intent.putExtra("finishflag", false);
                        getSharedPreferences(SHOW_INTRO, MODE_PRIVATE).edit().putBoolean("flag", false).commit();
                    }
                    else
                    {
                        intent = new Intent(LoginActivity.this, HomeActivity.class);
                    }
                    startActivity(intent);
                    finish();
                default:
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.fetch_success), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
