package edu.pku.assistant;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.pku.assistant.R;
import edu.pku.assistant.Tool.Constants;
import edu.pku.assistant.Tool.CustomerHttpClient;

public class UserInfoActivity extends Activity {
    private TextView NameTextView;
    private TextView UserIdTextView;
    private ListView BlogListView;
    private ListView StateListView;

    private String renrenid;
    private String name;
    private String keyword;
    private ArrayList<HashMap<String, Object>> BlogList;
    private ArrayList<HashMap<String, Object>> StateList;

    private SimpleAdapter blogAdapter;
    private SimpleAdapter stateAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        renrenid = intent.getStringExtra("userid");
        keyword = intent.getStringExtra("keyword");
        BlogList = new ArrayList<HashMap<String, Object>>();
        BlogList.clear();
        StateList = new ArrayList<HashMap<String, Object>>();
        StateList.clear();

        setTitle(name);

        NameTextView = (TextView) findViewById(R.id.name);
        UserIdTextView = (TextView) findViewById(R.id.userid);
        BlogListView = (ListView) findViewById(R.id.bloglist);
        StateListView = (ListView) findViewById(R.id.statelist);
        NameTextView.setText(name);
        UserIdTextView.setText(renrenid);

        blogAdapter = new SimpleAdapter(UserInfoActivity.this,
                BlogList,
                R.layout.blog_item,
                new String[]{"title", "indexid"},
                new int[]{R.id.title, R.id.content});
        BlogListView.setAdapter(blogAdapter);
        BlogListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map = BlogList.get(i);
                Intent intent = new Intent(UserInfoActivity.this, BlogActivity.class);
                intent.putExtra("indexid", (String)map.get("indexid"));
                intent.putExtra("title", (String)map.get("title"));
                startActivity(intent);
            }
        });

        stateAdapter = new SimpleAdapter(UserInfoActivity.this,
                StateList,
                R.layout.blog_item,
                new String[]{"title", "indexid"},
                new int[]{R.id.title, R.id.content});
        StateListView.setAdapter(stateAdapter);
        StateListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map = StateList.get(i);
                Intent intent = new Intent(UserInfoActivity.this, BlogActivity.class);
                intent.putExtra("indexid", (String)map.get("indexid"));
                intent.putExtra("title", (String)map.get("title"));
                startActivity(intent);
            }
        });

        TabHost tabhost = (TabHost) findViewById(R.id.tabHost);
        tabhost.setup();
        TabHost.TabSpec tabstate = tabhost.newTabSpec("tabstate");
        tabstate.setIndicator(getResources().getString(R.string.related_tweets)).setContent(R.id.tabstate);
        tabhost.addTab(tabstate);
        TabHost.TabSpec tabblog = tabhost.newTabSpec("tabblog");
        tabblog.setIndicator(getResources().getString(R.string.related_posts)).setContent(R.id.tabblog);
        tabhost.addTab(tabblog);
        new DetailFetchTask(renrenid, keyword).execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
    }

    public class DetailFetchTask extends AsyncTask<Void, Void, Integer> {
        private String userId;
        private String keyWord;

        DetailFetchTask(String userid, String keyword) {
            userId = userid;
            keyWord = keyword;
        }

        @Override
        protected Integer doInBackground(Void... param) {
            Integer retValue = -1;
            String respond = null;

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("renrenid", renrenid));
            params.add(new BasicNameValuePair("key", keyWord));
            try {
                respond = CustomerHttpClient.post(Constants.base_url + "contact/index.php/search/searchUser", params);
            } catch (RuntimeException e) {
                respond = null;
            }
            if (respond == null) {
                retValue = -1;
            } else {
                try {
                    JSONObject jsonInfo = new JSONObject(respond);
                    int result = jsonInfo.getInt("result");
                    if (result == 0) retValue = 0;
                    else {
                        int count = jsonInfo.getInt("count");
                        JSONArray resultlist = jsonInfo.getJSONArray("sources");
                        for (int i = 0; i < count; i++) {
                            HashMap<String, Object> map = new HashMap<String, Object>();
                            map.put("title", resultlist.getJSONObject(i).getString("content"));
                            map.put("indexid", resultlist.getJSONObject(i).getString("indexid"));
                            int type = resultlist.getJSONObject(i).getInt("type");
                            if (type == 1) BlogList.add(map);
                            else StateList.add(map);
                        }
                        retValue = count;
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
                    Toast.makeText(UserInfoActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                    break;
                case 0:
                    blogAdapter.notifyDataSetChanged();
                    stateAdapter.notifyDataSetChanged();
                    break;
                default:
                    blogAdapter.notifyDataSetChanged();
                    stateAdapter.notifyDataSetChanged();
                    break;
            }
        }
    }
}
