package edu.pku.assistant.Group;

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
import com.afollestad.cardsui.CardTheme;

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
import edu.pku.assistant.Group.recommendation.RecommendCondition;
import edu.pku.assistant.Group.util.Group;
import edu.pku.assistant.Group.util.TimeSeg;
import edu.pku.assistant.R;
import edu.pku.assistant.Tool.Constants;
import edu.pku.assistant.Tool.Contact;
import edu.pku.assistant.Tool.CustomerHttpClient;
import edu.pku.assistant.Tool.Database;

@SuppressWarnings("unchecked")
public class GroupActivity extends FragmentActivity {

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

    private static final int INDEX_TIME = 0;
    private static int INDEX_MEMBER = 2;

    private static final int REQUEST_EDIT = 1;
    private static final int REQUEST_REC = 2;

    private CardAdapter adapter;

//    private ArrayList<Card> times = new ArrayList<Card>();

    private Group groupInfo = null;
    private ArrayList<TimeSeg> rendezvous = new ArrayList<TimeSeg>();

    private ArrayList<Contact> members;

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
        setContentView(R.layout.activity_group);

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        isNew = bundle.getBoolean("newGroup");
        groupInfo = (Group) bundle.getSerializable("groupInfo");
        rendezvous = groupInfo.rendezvous;

        tvGroupName = (TextView) findViewById(R.id.tv_group_name);
        tvMemNum = (TextView) findViewById(R.id.tv_member_num);
        tvCreator = (TextView) findViewById(R.id.tv_creator);
        tvCreateDate = (TextView) findViewById(R.id.tv_create_date);

        userId = getSharedPreferences("account", MODE_PRIVATE).getInt("userid", -1);

        if (isNew) {
            groupInfo.creatorId = userId;
            groupInfo.creatorName = getSharedPreferences("account", MODE_PRIVATE).getString("username", "unknown");
            AsyncTask task = new FetchCreatorName(String.valueOf(groupInfo.creatorId)).execute();
            try {
                task.get();
                Intent intent = new Intent(GroupActivity.this, GroupEdit.class);
                Bundle local_bundle = new Bundle();
                local_bundle.putBoolean("edit_mode", false);
                local_bundle.putSerializable("group", groupInfo);
                members = new ArrayList<Contact>();
//                members.add(new Contact(groupInfo.creatorId, groupInfo.creatorName));
                local_bundle.putSerializable("members", members);
                intent.putExtras(local_bundle);
                startActivityForResult(intent, REQUEST_EDIT);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            members = getMembers();
            initView();
        }

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
        db.Close();
        return members;
    }

    private void initView() {

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (groupInfo.groupId != -1) {
            tvGroupName.setText(groupInfo.groupName);
            tvMemNum.setText(String.valueOf(members.size()+1));
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

        CardHeader chTime = new CardHeader(GroupActivity.this.getResources().getString(R.string.appointment));
        CardHeader chMember = new CardHeader(GroupActivity.this.getResources().getString(R.string.group_member));

        adapter.add(chTime);

        if (rendezvous == null) {
//            times.add(new Card(GroupActivity.this.getResources().getString(R.string.determine_time), GroupActivity.this.getResources().getString(R.string.time_undetermined)));
            adapter.add(new Card(GroupActivity.this.getResources().getString(R.string.determine_time), GroupActivity.this.getResources().getString(R.string.time_undetermined)));
//            adapter.add(times.get(0));
        } else {
            Card time;
            for (TimeSeg ren : rendezvous) {
                time = new Card(ren.getDate() + ' ' + ren.getSeg(), ren.getDes());
//                times.add(time);
                adapter.add(time);
            }
            time = new Card(GroupActivity.this.getResources().getString(R.string.add_time), GroupActivity.this.getResources().getString(R.string.time_undetermined));
//            times.add(time);
            adapter.add(time);
        }

        INDEX_MEMBER = adapter.getCount();

        adapter.add(chMember);


        Card tmp = new CardCompressed(groupInfo.creatorName, GroupActivity.this.getResources().getString(R.string.creator));
        adapter.add(tmp);
        if (members != null) {
            for (Contact m : members) {
                Card mc = new CardCompressed(m.getName(), "");
                adapter.add(mc);
            }
        }

        list.setOnCardClickListener(new CardListView.CardClickListener() {
            @Override
            public void onCardClick(int index, CardBase card, View view) {
                Log.d("cardclick", "index: " + index + ", INDEX_MEMBER: " + INDEX_MEMBER);
                if (INDEX_TIME < index && index < INDEX_MEMBER) { // time
                    Intent intent = new Intent(GroupActivity.this, RecommendCondition.class);
                    Bundle bundle = new Bundle();
                    if (index == INDEX_MEMBER-1) {
                        bundle.putBoolean("newseg", true);
                        bundle.putString("description", getResources().getString(R.string.ts_description));
                    }
                    else {
                        bundle.putBoolean("newseg", false);
                        bundle.putString("description", ((Card)adapter.getItem(index)).getContent().toString());
                    }

                    bundle.putSerializable("members", members);
                    bundle.putInt("groupId", groupInfo.groupId);
                    bundle.putInt("timesegId", index-1);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, REQUEST_REC);
                    overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                } else if (index > INDEX_MEMBER+1) { // member info
                    Intent intent = new Intent(GroupActivity.this, ContactInfoActivity.class);
                    intent.putExtra("contact", members.get(index-INDEX_MEMBER-2));
                    startActivity(intent);
                    overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (userId == groupInfo.creatorId) {
            getMenuInflater().inflate(R.menu.group_manager, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit: {
                Intent intent = new Intent(GroupActivity.this, GroupEdit.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean("edit_mode", true);
                bundle.putSerializable("group", groupInfo);
                bundle.putSerializable("members", members);
                intent.putExtras(bundle);
                startActivityForResult(intent, REQUEST_EDIT);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                break;
            }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RecommendCondition.RESULT_CHANGE) {
            Group old_group = new Group(groupInfo);
            int timesegId = data.getExtras().getInt("timesegId");
            TimeSeg ts = groupInfo.rendezvous.get(timesegId);
            ts.description = data.getExtras().getString("description");
//            times.get(timesegId).setContent(ts.getDes());
            Card card = (Card)adapter.getItem(timesegId+1);
            card.setContent(ts.getDes());
            adapter.notifyDataSetChanged();
            groupInfo.rendezvous.set(timesegId, ts);
            updateDataBase(old_group, groupInfo);
            new EditGroupTask(String.valueOf(groupInfo.creatorId), groupInfo).execute();
        } else if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_REC) { // set time
                int timesegId = data.getExtras().getInt("timesegId");
                TimeSeg ts = (TimeSeg) data.getExtras().getSerializable("result");
                ts.description = data.getExtras().getString("description");
                Log.d("recommend_result", "timesegId: " + timesegId);
                Card card = (Card)adapter.getItem(timesegId + 1);
//                times.get(timesegId).setTitle(ts.getDate() + ' ' + ts.getSeg());
//                times.get(timesegId).setContent(ts.getDes());
                card.setTitle(ts.getDate() + ' ' + ts.getSeg());
                card.setContent(ts.getDes());
                adapter.notifyDataSetChanged();

                Group old_group = new Group(groupInfo);

                if (timesegId == groupInfo.rendezvous.size()) {
                    // a new time segment
                    groupInfo.rendezvous.add(ts);
                    Card time = new Card(GroupActivity.this.getResources().getString(R.string.add_time), GroupActivity.this.getResources().getString(R.string.time_undetermined));
//                    times.add(timesegId+1, time);
                    adapter.add(timesegId+2, time);
                    adapter.notifyDataSetChanged();
                    INDEX_MEMBER++;
                } else {
                    groupInfo.rendezvous.set(timesegId, ts);
                }

                Database db = new Database(GroupActivity.this);
                db.Open();
                db.UpdateGroupTime(groupInfo.groupId, Group.getBeginTimes(rendezvous), Group.getEndTimes(rendezvous));
                db.Close();

                updateDataBase(old_group, groupInfo);
                new AppointmentTask(String.valueOf(groupInfo.groupId), Group.getBeginTimes(rendezvous), Group.getEndTimes(rendezvous)).execute();

            } else if (requestCode == REQUEST_EDIT) { // edit group
                Group group = (Group) data.getExtras().getSerializable("group");
                members = (ArrayList<Contact>) data.getExtras().getSerializable("members");
                changes = (HashMap<Contact, Integer>) data.getExtras().getSerializable("member_change");
                initView();

                if (isNew) {
                    isNew = false;
                    groupInfo.groupName = group.groupName;
                    groupInfo.creatorId = group.creatorId;
                    groupInfo.creatorName = group.creatorName;
                    try {
                        AsyncTask newTask = new NewGroupTask(String.valueOf(groupInfo.creatorId), groupInfo).execute();
                        newTask.get(1000, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    }
                } else {
                    updateDataBase(groupInfo, group);
                    groupInfo.groupName = group.groupName;
                    groupInfo.creatorId = group.creatorId;
                    groupInfo.creatorName = group.creatorName;
                    try {
                        AsyncTask editTask = new EditGroupTask(String.valueOf(groupInfo.creatorId), groupInfo).execute();
                        editTask.get(1000, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    }
                }
                tvGroupName.setText(groupInfo.groupName);
                tvMemNum.setText(String.valueOf(members.size()));
                tvCreator.setText(groupInfo.creatorName);
                tvCreateDate.setText(sdf.format(groupInfo.createDate.getTime()));
            }
        } else {
            if (resultCode == RESULT_CANCELED) {
                if (requestCode == REQUEST_EDIT) {
                    if (isNew) {
                        finish();
                        overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
                    } else {
                        Boolean removed = data.getExtras().getBoolean("removed");
                        if (removed) {
//                            removedFromDataBase(groupInfo);
                            try {
                                AsyncTask task = new RemoveGroupTask(String.valueOf(groupInfo.groupId)).execute();
                                task.get(1000, TimeUnit.MILLISECONDS); // wait at most 1000ms for delete result.
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (TimeoutException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    private void removedFromDataBase(Group groupInfo) {
        Database db = new Database(this);
        db.Open();
        db.DeleteGroup(groupInfo.formatGroupName());
        db.Close();
    }

    private void updateDataBase(Group oldGroup, Group newGroup) {
        Database db = new Database(this);
        db.Open();
        db.UpdateGroup(oldGroup.formatGroupName(), newGroup.formatGroupName());
        db.Close();
    }

    private void addToDataBase(Group groupInfo) {
        Database db = new Database(this);
        db.Open();
        db.InsertGroup(groupInfo.groupId, groupInfo.formatGroupName(), String.valueOf(groupInfo.creatorId), String.valueOf(groupInfo.createDate.getTimeInMillis() / 1000));
        db.Close();
    }



    public class AppointmentTask extends AsyncTask<Void, Void, Integer> {
        private String groupId;
        private String beginDate;
        private String endDate;
        private String msg;

        AppointmentTask(String groupid, String begindate, String enddate) {
            groupId = groupid;
            beginDate = begindate;
            endDate = enddate;
        }

        @Override
        protected Integer doInBackground(Void... param) {
            Integer retValue = -1;
            String respond;

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("groupid", groupId));
            params.add(new BasicNameValuePair("begintime", beginDate));
            params.add(new BasicNameValuePair("endtime", endDate));
            try {
                respond = CustomerHttpClient.post(Constants.base_url + "contact/index.php/group/updatetime", params);
            } catch (RuntimeException e) {
                respond = null;
            }
            if (respond == null) {
                retValue = -1;
            } else {
                JSONObject jsonInfo;
                try {
                    jsonInfo = new JSONObject(respond);
                    retValue = jsonInfo.getInt("result");
                    msg = jsonInfo.getString("msg");
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
                    Toast.makeText(GroupActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(GroupActivity.this, getResources().getString(R.string.appoint_success), Toast.LENGTH_SHORT).show();
                    new EditGroupTask(String.valueOf(groupInfo.creatorId), groupInfo).execute();
                    break;
            }
        }
    }

    public class NewGroupTask extends AsyncTask<Void, Void, Integer> {
        private String userId;
        private Group group;
        private StringBuilder memberIds;
        private String msg;

        NewGroupTask(String userid, Group groupinfo) {
            userId = userid;
            group = groupinfo;
            memberIds = new StringBuilder("");
            if (members.size() > 0) {
                for (Contact m : members) {
                    memberIds.append(m.getId()).append(",");
                }
                memberIds.deleteCharAt(memberIds.length() - 1);
            }
        }

        @Override
        protected Integer doInBackground(Void... param) {
            Integer retValue = -1;
            String respond;

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("userid", userId));
            params.add(new BasicNameValuePair("name", String.valueOf(group.formatGroupName())));
            params.add(new BasicNameValuePair("num", String.valueOf(members.size())));
            params.add(new BasicNameValuePair("createuser", userId));
            if (memberIds.length() > 0) {
                params.add(new BasicNameValuePair("memberids", memberIds.toString()));
            }
            try {
                respond = CustomerHttpClient.post(Constants.base_url + "contact/index.php/group/addgroup", params);
            } catch (RuntimeException e) {
                respond = null;
            }
            if (respond == null) {
                retValue = -1;
            } else {
                JSONObject jsonInfo;
                try {
                    jsonInfo = new JSONObject(respond);
                    retValue = jsonInfo.getInt("result");
                    msg = jsonInfo.getString("msg");
                    groupInfo.groupId = jsonInfo.getInt("groupid");
                    groupInfo.createDate.setTime(new Date(Long.parseLong(String.valueOf(jsonInfo.getInt("createtime")) + "000")));
                    addToDataBase(groupInfo);

                    JSONArray membersArray = jsonInfo.getJSONArray("membersinfo");
                    if (changes != null) {
                        int i = 0;
                        for (Map.Entry<Contact, Integer> entry : changes.entrySet()) {
                            if (entry.getValue() > 0) {
                                entry.getKey().setUserId(membersArray.getJSONObject(i).getInt("userid"));
                                entry.getKey().setUserName(membersArray.getJSONObject(i).getString("username"));
                                entry.getKey().setPhone(membersArray.getJSONObject(i).getString("phone"));
                                i++;
                            }
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
            switch(success) {
                case -1:
                    Toast.makeText(GroupActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    if (changes != null && changes.size() > 0) {
                        Database db = new Database(GroupActivity.this);
                        db.Open();
                        for (Map.Entry<Contact, Integer> entry : changes.entrySet()) {
                            if (entry.getValue() > 0) {
                                db.InsertContacts(groupInfo.groupId, entry.getKey().getName(), entry.getKey().getId(),
                                        entry.getKey().getUserName(), entry.getKey().getUserId(), entry.getKey().getPhone());
                            } else if (entry.getValue() < 0) {
                                db.DeleteContactsFromGroup(String.valueOf(entry.getKey().getId()), groupInfo.formatGroupName());
                            }
                        }
                        db.Close();
                    }

                default:
//                    Toast.makeText(GroupActivity.this, msg, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    public class EditGroupTask extends AsyncTask<Void, Void, Integer> {
        private String userId;
        private Group groupInfo;
        private StringBuilder memberIds;
        private String msg;

        EditGroupTask(String userid, Group groupinfo) {
            userId = userid;
            groupInfo = groupinfo;
            memberIds = new StringBuilder("");
            if (members.size() > 0) {
                for (Contact m : members) {
                    memberIds.append(m.getId()).append(",");
                }
                memberIds.deleteCharAt(memberIds.length() - 1);
            }
        }

        @Override
        protected Integer doInBackground(Void... param) {
            Integer retValue = -1;
            String respond;

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("groupid", String.valueOf(groupInfo.groupId)));
            params.add(new BasicNameValuePair("name", String.valueOf(groupInfo.formatGroupName())));
            params.add(new BasicNameValuePair("num", String.valueOf(members.size())));
            params.add(new BasicNameValuePair("createuser", String.valueOf(userId)));
            if (memberIds.length() > 0) {
                params.add(new BasicNameValuePair("memberids", memberIds.toString()));
            }
            try {
                respond = CustomerHttpClient.post(Constants.base_url + "contact/index.php/group/updategroup", params);
                Log.d("testtest", respond);
            } catch (RuntimeException e) {
                respond = null;
            }
            if (respond == null) {
                retValue = -1;
            } else {
                JSONObject jsonInfo;
                try {
                    jsonInfo = new JSONObject(respond);
                    retValue = jsonInfo.getInt("result");
                    msg = jsonInfo.getString("msg");

                    JSONArray membersArray = jsonInfo.getJSONArray("membersinfo");
                    if (changes != null) {
                        int i = 0;
                        for (Map.Entry<Contact, Integer> entry : changes.entrySet()) {
                            if (entry.getValue() > 0) {
                                entry.getKey().setUserId(membersArray.getJSONObject(i).getInt("userid"));
                                entry.getKey().setUserName(membersArray.getJSONObject(i).getString("username"));
                                entry.getKey().setPhone(membersArray.getJSONObject(i).getString("phone"));
                                i++;
                            }
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
            switch(success) {
                case -1:
                    Toast.makeText(GroupActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    if (changes != null && changes.size() > 0) {
                        Database db = new Database(GroupActivity.this);
                        db.Open();
                        for (Map.Entry<Contact, Integer> entry : changes.entrySet()) {
                            if (entry.getValue() > 0) {
                                db.InsertContacts(groupInfo.groupId, entry.getKey().getName(), entry.getKey().getId(),
                                        entry.getKey().getUserName(), entry.getKey().getUserId(), entry.getKey().getPhone());
                            } else if (entry.getValue() < 0) {
                                db.DeleteContactsFromGroup(String.valueOf(entry.getKey().getId()), groupInfo.formatGroupName());
                            }
                        }
                        db.Close();
//                    Toast.makeText(GroupActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }

    public class RemoveGroupTask extends AsyncTask<Void, Void, Integer> {
        private String groupId;
        private String msg;

        RemoveGroupTask(String groupId) {
            this.groupId = groupId;
        }

        @Override
        protected Integer doInBackground(Void... param) {
            Integer retValue = -1;
            String respond;

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("groupid", groupId));
            try {
                respond = CustomerHttpClient.post(Constants.base_url + "contact/index.php/group/deletegroup", params);
            } catch (RuntimeException e) {
                respond = null;
            }
            if (respond == null) {
                retValue = -1;
            } else {
                JSONObject jsonInfo;
                try {
                    jsonInfo = new JSONObject(respond);
                    retValue = jsonInfo.getInt("result");
                    msg = jsonInfo.getString("msg");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return retValue;
        }

        @Override
        protected void onPostExecute(Integer success) {
            switch (success) {
                case -1:
                    Toast.makeText(GroupActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    removedFromDataBase(groupInfo);
                default:
                    Toast.makeText(GroupActivity.this, getResources().getString(R.string.remove_group_success), Toast.LENGTH_SHORT).show();
                    finish();
                    overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
                    break;
            }
        }
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
                    Toast.makeText(GroupActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    tvCreator.setText(groupInfo.creatorName);
                    break;
            }
        }
    }
}
