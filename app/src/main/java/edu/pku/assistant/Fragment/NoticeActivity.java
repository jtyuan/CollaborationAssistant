package edu.pku.assistant.Fragment;

import android.app.ActionBar;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.cardsui.Card;
import com.afollestad.cardsui.CardAdapter;
import com.afollestad.cardsui.CardBase;
import com.afollestad.cardsui.CardCompressed;
import com.afollestad.cardsui.CardHeader;
import com.afollestad.cardsui.CardListView;
import com.beardedhen.androidbootstrap.BootstrapButton;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import edu.pku.assistant.ContactInfoActivity;
import edu.pku.assistant.Group.GroupEdit;
import edu.pku.assistant.Group.recommendation.RecommendCondition;
import edu.pku.assistant.Group.util.Group;
import edu.pku.assistant.Group.util.TimeSeg;
import edu.pku.assistant.R;
import edu.pku.assistant.Tool.Constants;
import edu.pku.assistant.Tool.Contact;
import edu.pku.assistant.Tool.CustomerHttpClient;
import edu.pku.assistant.Tool.Database;

@SuppressWarnings("unchecked")
public class NoticeActivity extends FragmentActivity {

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

    private static final int INDEX_TIME = 0;
    private static int INDEX_MEMBER = 2;

    private static final int REQUEST_EDIT = 1;
    private static final int REQUEST_REC = 2;

    private CardAdapter adapter;

    private ArrayList<Card> times = new ArrayList<Card>();

    private Group groupInfo = null;
    private ArrayList<TimeSeg> rendezvous = new ArrayList<TimeSeg>();

    private ArrayList<Contact> members;
    private ArrayList<Group> l = new ArrayList<Group>();

    private boolean isNew = false;

    private HashMap<Contact, Integer> changes;
    private TextView tvGroupName;
    private TextView tvMemNum;
    private TextView tvCreateDate;
    private TextView tvCreator;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_with_button);

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        int index = bundle.getInt("groupid");

        initDB();

        groupInfo = l.get(index);
        rendezvous = groupInfo.rendezvous;
        tvGroupName = (TextView) findViewById(R.id.tv_group_name);
        tvMemNum = (TextView) findViewById(R.id.tv_member_num);
        tvCreator = (TextView) findViewById(R.id.tv_creator);
        tvCreateDate = (TextView) findViewById(R.id.tv_create_date);

        userId = getSharedPreferences("account", MODE_PRIVATE).getInt("userid", -1);
        members = getMembers();
        initView();

    }

    private ArrayList<Contact> getMembers() {
        ArrayList<Contact> members = new ArrayList<Contact>();
        Database db = new Database(this);
        db.Open();
        Cursor cursor = db.getGroupMembers(groupInfo.formatGroupName());
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Cursor local_cursor = db.getContactById(cursor.getInt(0));
            local_cursor.moveToFirst();
            Contact member = new Contact(local_cursor.getInt(1), local_cursor.getString(0));
            members.add(member);
            cursor.moveToNext();
        }
        return members;
    }

    private void initView() {

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (groupInfo.groupId != -1) {
            tvGroupName.setText(groupInfo.groupName);
            tvMemNum.setText(String.valueOf(members.size()));
            AsyncTask task = new FetchCreatorName(String.valueOf(groupInfo.creatorId)).execute();
            try {
                task.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            tvCreateDate.setText(sdf.format(groupInfo.createDate.getTime()));
        }

        CardListView list = (CardListView) findViewById(R.id.card_list);
        adapter = new CardAdapter(this, android.R.color.holo_blue_dark);
        list.setAdapter(adapter);

        CardHeader chTime = new CardHeader(NoticeActivity.this.getResources().getString(R.string.appointment));
        CardHeader chMember = new CardHeader(NoticeActivity.this.getResources().getString(R.string.group_member));

        adapter.add(chTime);
        if (rendezvous != null) {
            Card time;
            for (TimeSeg ren : rendezvous) {
                time = new Card(ren.getDate() + ' ' + ren.getSeg(), ren.getDes());
                times.add(time);
                adapter.add(time);
            }
        }

        INDEX_MEMBER = adapter.getCount();

        adapter.add(chMember);


        if (members != null) {
            boolean first = true;
            for (Contact m : members) {
                Card mc;
                if (first) {
                    mc = new CardCompressed(m.getName(), NoticeActivity.this.getResources().getString(R.string.creator));
                    first = false;
                } else {
                    mc = new Card(m.getName());
                }
                adapter.add(mc);
            }
        }

        BootstrapButton button_confirm = (BootstrapButton) findViewById(R.id.button_confirm);
        button_confirm.setText(NoticeActivity.this.getResources().getString(R.string.req_confirm));
        BootstrapButton button_cancel = (BootstrapButton) findViewById(R.id.button_cancel);
        button_cancel.setText(NoticeActivity.this.getResources().getString(R.string.req_cancel));

        list.setOnCardClickListener(new CardListView.CardClickListener() {
            @Override
            public void onCardClick(int index, CardBase card, View view) {
                Log.d("cardclick", "index: " + index + ", INDEX_MEMBER: " + INDEX_MEMBER);
                if (index > INDEX_MEMBER) { // member info
                    Intent intent = new Intent(NoticeActivity.this, ContactInfoActivity.class);
                    intent.putExtra("contact", members.get(index-INDEX_MEMBER-1));
                    startActivity(intent);
                    overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                }
            }
        });

        button_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(NoticeActivity.this, "接受请求，加入小组", Toast.LENGTH_SHORT).show();
                finish();
                overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
            }
        });

        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
    }

    public class FetchCreatorName extends AsyncTask<Void, Void, Integer> {
        private String userId;
        private String msg;

        FetchCreatorName(String userId) {
            this.userId = userId;
        }

        @Override
        protected Integer doInBackground(Void... param) {
            Integer retValue = -1;
            String respond;

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("userid", userId));
            try {
                respond = CustomerHttpClient.post(Constants.base_url + "contact/index.php/group/getname", params);
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
                        groupInfo.creatorName = jsonInfo.getString("name");
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
                    Toast.makeText(NoticeActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    tvCreator.setText(groupInfo.creatorName);
                    break;
            }
        }
    }

    private void initDB() {
        Database db = new Database(this);
        db.Open();
        Cursor cursor = db.getGroups();
        cursor.moveToFirst();
        l.clear();
        while (!cursor.isAfterLast()) {
            Calendar calendar = Calendar.getInstance();
            String[] title_raw = cursor.getString(1).split(",");
            String beginTime = cursor.getString(5), endTime = cursor.getString(6);
            String[] beginTimes = null, endTimes = null;
            if (beginTime != null && endTime != null) {
                beginTimes = beginTime.split(",");
                endTimes = endTime.split(",");
            }
            calendar.setTime(new Date(Long.valueOf(cursor.getString(4) + "000")));
            Group group;
            ArrayList<TimeSeg> rendezvous = new ArrayList<TimeSeg>();
            if (beginTimes != null) {
                for (int i = 0; i < beginTimes.length; ++i) {
                    Calendar beginDate = Calendar.getInstance(), endDate = Calendar.getInstance();
                    beginDate.setTime(new Date(Long.valueOf(beginTimes[i] + "000")));
                    endDate.setTime(new Date(Long.valueOf(endTimes[i] + "000")));
                    String desc = getResources().getString(R.string.ts_description);
                    if (i+1 < title_raw.length)
                        desc = title_raw[i+1];
                    rendezvous.add(new TimeSeg(beginDate, endDate, desc));
                }
                group = new Group(cursor.getInt(0), title_raw[0], Integer.parseInt(cursor.getString(3)), calendar, rendezvous);
            } else {
                group = new Group(cursor.getInt(0), title_raw[0], Integer.parseInt(cursor.getString(3)), calendar);
            }

            l.add(group);
            cursor.moveToNext();
        }
        db.Close();
    }
}



