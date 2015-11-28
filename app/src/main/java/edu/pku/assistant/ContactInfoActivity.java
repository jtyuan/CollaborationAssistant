package edu.pku.assistant;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.pku.assistant.Tool.Constants;
import edu.pku.assistant.Tool.Contact;
import edu.pku.assistant.Tool.CustomerHttpClient;


public class ContactInfoActivity extends Activity {

    private static final int[] ContactIcon= {0, R.drawable.logo_wechat, R.drawable.logo_weibo, R.drawable.logo_qq, R.drawable.logo_renren, R.drawable.logo_phone, R.drawable.logo_sms};

    private static final int[] ContactText= {0, R.string.weixin, R.string.weibo, R.string.qq, R.string.renren, R.string.phone, R.string.sms};

    private Contact contact;

    private LinearLayout rootLinearLayout;

    private TextView NameTextView;

    private TextView PhoneTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_info);

        contact = (Contact)getIntent().getSerializableExtra("contact");
        setTitle(contact.getName());

        rootLinearLayout = (LinearLayout)findViewById(R.id.recommended_contact);

        NameTextView = (TextView)findViewById(R.id.name);
        PhoneTextView = (TextView)findViewById(R.id.phone);

        NameTextView.setText(contact.getName());
        PhoneTextView.setText(contact.getPhone());

        int userid = getSharedPreferences("account", MODE_PRIVATE).getInt("userid", 0);
        if (contact.getUserId() != -1)
            new GetContactTask(getApplicationContext(), userid).execute();
        else if (!contact.getPhone().equals("")) {
            addContactView(5);
            addContactView(6);
        } else {
            addContactView(4);
        }
    }


    public class GetContactTask extends AsyncTask<Void, Void, Integer> {
        private Context context;
        private int UserID;
        private ArrayList<Integer> ContactList;
        private String msg;

        GetContactTask(Context context, int userid) {
            this.context = context;
            this.UserID = userid;
            ContactList = new ArrayList<Integer>();
        }

        @Override
        protected Integer doInBackground(Void... param) {
            Integer retValue = -1;
            String respond = null;

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("userid", String.valueOf(UserID)));
            try {
                respond = CustomerHttpClient.post(Constants.base_url + "contact/index.php/contact/matchquery", params);
            } catch (RuntimeException e) {
                respond = null;
            }
            if (respond == null) {
                retValue = -1;
            } else {
                try {
                    JSONObject jsonInfo = new JSONObject(respond);
                    int result = jsonInfo.getInt("result");
                    if (result == 1) {
                        JSONArray contactlist = jsonInfo.getJSONArray("contactlist");
                        for (int i = 0; i < contactlist.length(); i++) {
                            ContactList.add(contactlist.getJSONObject(i).getInt("id"));
                        }
                        retValue = 1;
                    }
                    else
                    {
                        msg = jsonInfo.getString("msg");
                        retValue = 0;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return retValue;
        }
        @Override
        protected void onPostExecute(Integer retValue) {
            switch (retValue)
            {
                case 0:
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    for (int i = 0; i < ContactList.size(); i++) {
                        int contactid = ContactList.get(i);
                        addContactView(contactid);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public void addContactView(final int contactid) {
        LinearLayout ContactLinearLayout = (LinearLayout)getLayoutInflater().inflate(R.layout.view_contact, null);
        ImageView contactIcon = (ImageView)ContactLinearLayout.findViewById(R.id.icon);
        final TextView contactText = (TextView)ContactLinearLayout.findViewById(R.id.contact);
        contactIcon.setImageResource(ContactIcon[contactid]);
        contactText.setText(ContactText[contactid]);
        LinearLayout Divider = (LinearLayout)getLayoutInflater().inflate(R.layout.view_divider, null);
        ContactLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                switch(contactid) {
                    case 1:
                        startAPP("com.tencent.mm");
                        break;
                    case 3:
                        startAPP("com.tencent.mobileqq");
                        break;
                    case 4:
                        startAPP("com.renren.mobile.android");
                        break;
                    case 5:
                        intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contact.getPhone()));
                        startActivity(intent);
                        break;
                    case 6:
                        intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + contact.getPhone()));
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            }
        });

        rootLinearLayout.addView(ContactLinearLayout);
        rootLinearLayout.addView(Divider);
    }

    public void startAPP(String appPackageName){
        try{
            Intent intent = this.getPackageManager().getLaunchIntentForPackage(appPackageName);
            startActivity(intent);
        }catch(Exception e){
            Toast.makeText(this, "没有安装", Toast.LENGTH_LONG).show();
        }
    }
}
