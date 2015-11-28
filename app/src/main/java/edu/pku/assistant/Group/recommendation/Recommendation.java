package edu.pku.assistant.Group.recommendation;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.cardsui.CardBase;
import com.afollestad.cardsui.CardHeader;
import com.afollestad.cardsui.CardListView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.pku.assistant.Group.util.TimeSeg;
import edu.pku.assistant.Group.util.adapter.TimeList.TimeListAdapter;
import edu.pku.assistant.R;
import edu.pku.assistant.Tool.Constants;
import edu.pku.assistant.Tool.Contact;
import edu.pku.assistant.Tool.CustomerHttpClient;


@SuppressWarnings("unchecked")
public class Recommendation extends FragmentActivity {

    private CardListView list;
    public static TimeListAdapter adapter;

    public static ArrayList<Contact> members;
    public static int memberNum;

    private static final int MORNING_START = 8;
    private static final int MORNING_END = 12;
    private static final int AFTERNOON_START = 12;
    private static final int AFTERNOON_END = 18;
    private static final int ANYTIME_START = MORNING_START;
    private static final int ANYTIME_END = AFTERNOON_END;

    private ArrayList<TimeSeg> l = new ArrayList<TimeSeg>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        list = (CardListView) findViewById(R.id.card_list);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            int groupId = bundle.getInt("groupId");
            members = (ArrayList<Contact>) bundle.getSerializable("members");
            memberNum = members.size();
            TimeSeg condition = (TimeSeg) bundle.getSerializable("condition");
            new FetchFreeDateList(groupId, condition).execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recommendation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @SuppressLint("NewApi")
    private void initializeAdapter() {

        adapter = new TimeListAdapter(this, android.R.color.holo_blue_dark, l);
        list.setAdapter(adapter);

        if (l.size() == 0)
            adapter.add(new CardHeader(this.getResources().getString(R.string.rec_no_result)));
        else
        list.setOnCardClickListener(new CardListView.CardClickListener() {
            @Override
            public void onCardClick(int index, CardBase card, View view) {
//                Intent intent = new Intent(Recommendation.this, ResultConfirm.class);
//                Bundle bundle = new Bundle();
//                bundle.putSerializable("item", adapter.getPrivateItem(index));
//                intent.putExtras(bundle);
//                startActivityForResult(intent, 0);
//                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                final int card_index = index;
                AlertDialog.Builder ad = new AlertDialog.Builder(Recommendation.this)
                        .setTitle(getResources().getString(R.string.hint))
                        .setMessage(R.string.rec_confirm_question)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton(getResources().getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent();
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("result", adapter.getPrivateItem(card_index).getTimeSeg());
                                intent.putExtras(bundle);
                                Recommendation.this.setResult(RESULT_OK, intent);
                                Recommendation.this.finish();
                                overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.button_cancel), null);
                ad.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
            finish();
            overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
        }
    }


    public class FetchFreeDateList extends AsyncTask<Void, Void, Integer> {
        private int groupId;
        private String freetime;
        private int interval;
        private String msg;

        FetchFreeDateList(int groupId, TimeSeg condition) {
            this.groupId = groupId;
            this.freetime = this.generateDateList(condition);
            if (condition.mode == RecommendCondition.MODE_CUSTOMED)
                this.interval = (condition.end.get(Calendar.HOUR_OF_DAY)
                        - condition.beg.get(Calendar.HOUR_OF_DAY)
                        + (
                            condition.end.get(Calendar.MINUTE)
                             - condition.beg.get(Calendar.MINUTE)
                          ) / 60) * 3600;
            else
                this.interval = (int)(condition.length * 3600);
        }

        @Override
        protected Integer doInBackground(Void... param) {
            Integer retValue = -1;
            String respond;

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("groupid", String.valueOf(groupId)));
            params.add(new BasicNameValuePair("interval", String.valueOf(interval)));
            if (freetime.length() > 0) {
                params.add(new BasicNameValuePair("freelist", freetime));
            }
            try {
                respond = CustomerHttpClient.post(Constants.base_url + "contact/index.php/group/getfreetime", params);
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

                    JSONArray freetimeArray = jsonInfo.getJSONArray("freetime");
                    for (int i = 0; i < freetimeArray.length(); ++i) {
                        Calendar starttime = Calendar.getInstance(), endtime = Calendar.getInstance();
                        starttime.setTimeInMillis(
                                Long.parseLong(String.valueOf(freetimeArray.getJSONObject(i).getInt("starttime") + "000")));
                        endtime.setTimeInMillis(
                                Long.parseLong(String.valueOf(freetimeArray.getJSONObject(i).getInt("endtime") + "000")));
                        TimeSeg ts = new TimeSeg(starttime, endtime, Recommendation.this.getResources().getString(R.string.ts_description));

/* TODO
                        JSONArray userArray = freetimeArray.getJSONObject(i).getJSONArray("userids");
                        for (int j = 0; j < userArray.length(); ++j)
                            ts.availUserIds.add(userArray.getJSONObject(j).getInt("userid"));
*/
                        // only add non-empty time segment to l
//                        if (ts.availUserIds.size() > 0)
                            l.add(ts);
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
//                    Toast.makeText(Recommendation.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                    Toast.makeText(Recommendation.this, msg, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    initializeAdapter();
                    break;
            }
        }


        private String generateDateList(TimeSeg condition) {
            ArrayList<TimeSeg> list = new ArrayList<TimeSeg>();
            Calendar s = condition.beg;
            Calendar t = condition.end;
            switch (condition.mode) {
                case RecommendCondition.MODE_MORNING:
                    s.set(Calendar.HOUR_OF_DAY, MORNING_START);
                    t.set(Calendar.HOUR_OF_DAY, MORNING_END);
                    break;
                case RecommendCondition.MODE_AFTERNOON:
                    s.set(Calendar.HOUR_OF_DAY, AFTERNOON_START);
                    t.set(Calendar.HOUR_OF_DAY, AFTERNOON_END);
                    break;
                case RecommendCondition.MODE_ANYTIME:
                    s.set(Calendar.HOUR_OF_DAY, ANYTIME_START);
                    t.set(Calendar.HOUR_OF_DAY, ANYTIME_END);
                    break;
                case RecommendCondition.MODE_CUSTOMED:
                    break;
            }

            Calendar c = Calendar.getInstance();
            for (c.setTime(s.getTime());
//                 c.get(Calendar.MONTH) <= t.get(Calendar.MONTH) && c.get(Calendar.DATE) <= t.get(Calendar.DATE);
                 c.compareTo(t) <= 0;
                 c.add(Calendar.DATE, 1)) {
                    Calendar beg = Calendar.getInstance();
                    Calendar end = Calendar.getInstance();
                    beg.setTime(c.getTime());
                    end.setTime(c.getTime());
                    end.set(Calendar.HOUR_OF_DAY, t.get(Calendar.HOUR_OF_DAY));
                    list.add(new TimeSeg(beg, end, getResources().getString(R.string.ts_description)));
            }
            // give StringBuilder a default length to improve performance
            StringBuilder times = new StringBuilder(1024);
            for (TimeSeg ts : list) {
                times.append(ts.beg.getTimeInMillis()/1000).append('-')
                        .append(ts.end.getTimeInMillis() / 1000).append(',');
            }
            if (times.length() > 0)
                times.deleteCharAt(times.length()-1);

            return times.toString();
        }

    }

}
